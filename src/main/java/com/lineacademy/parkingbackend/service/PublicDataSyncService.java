package com.lineacademy.parkingbackend.service;

import com.lineacademy.parkingbackend.domain.entity.ParkingLot;
import com.lineacademy.parkingbackend.dto.parking.PublicParkingDataDto;
import com.lineacademy.parkingbackend.dto.parking.response.PublicDataResponseWrapper;
import com.lineacademy.parkingbackend.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicDataSyncService {
    private final ParkingLotRepository parkingLotRepository;
    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                    .build())
            .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    @Value("${public.api.key}")
    private String apiKey;

    @Value("${public.api.url}")
    private String apiUrl;

    public Mono<Void> syncParkingLots() {
        return fetchAllFromPublicApi()
                .distinct(PublicParkingDataDto::getPrkplceNo)
                .flatMap(this::saveOrUpdateParkingLot, 5)
                .then();
    }

    private Flux<PublicParkingDataDto> fetchAllFromPublicApi() {
        int numOfRows = 1000;

        return fetchPage(1, numOfRows)
                .flatMapMany(firstPageWrapper -> {
                    if (!isValidResponse(firstPageWrapper)) {
                        return Flux.empty();
                    }

                    List<PublicParkingDataDto> firstPageItems = firstPageWrapper.getResponse().getBody().getItems();
                    int totalCount = firstPageWrapper.getResponse().getBody().getTotalCount();

                    int totalPages = (int) Math.ceil((double) totalCount / numOfRows);
                    log.info("총 주차장 데이터: {}건, 총 페이지 수: {}", totalCount, totalPages);

                    Flux<PublicParkingDataDto> firstPageFlux = Flux.fromIterable(firstPageItems)
                            .filter(this::hasValidCoordinates);

                    if (totalPages <= 1) {
                        return firstPageFlux;
                    }

                    Flux<PublicParkingDataDto> remainingPagesFlux = Flux.range(2, totalPages - 1)
                            .flatMap(pageNo -> fetchPage(pageNo, numOfRows)
                                            .filter(this::isValidResponse)
                                            // 💡 getResponse() 거치도록 수정 및 .getItem() 제거
                                            .map(wrapper -> wrapper.getResponse().getBody().getItems())
                                            .flatMapMany(Flux::fromIterable)
                                            .filter(this::hasValidCoordinates),
                                    5
                            );

                    return Flux.concat(firstPageFlux, remainingPagesFlux);
                });
    }

    private boolean hasValidCoordinates(PublicParkingDataDto dto) {
        return dto.getLatitude() != null && dto.getLongitude() != null
                && dto.getLatitude().doubleValue() > 0
                && dto.getLongitude().doubleValue() > 0;
    }

    private Mono<PublicDataResponseWrapper> fetchPage(int pageNo, int numOfRows) {
        log.info("API 호출 중 - 페이지: {}", pageNo);
        return webClient.get()
                .uri(apiUrl + "?serviceKey={serviceKey}&type=json&numOfRows={numOfRows}&pageNo={pageNo}",
                        apiKey, numOfRows, pageNo)
                .retrieve()
                .bodyToMono(PublicDataResponseWrapper.class)
                .onErrorResume(e -> {
                    log.error("공공데이터 API 통신 실패 (페이지 {}): ", pageNo, e);
                    return Mono.empty();
                });
    }

    private boolean isValidResponse(PublicDataResponseWrapper wrapper) {
        return wrapper != null
                && wrapper.getResponse() != null
                && wrapper.getResponse().getHeader() != null
                && wrapper.getResponse().getBody() != null
                && "00".equals(wrapper.getResponse().getHeader().getResultCode())
                && wrapper.getResponse().getBody().getItems() != null;
    }

    @Transactional
    protected Mono<ParkingLot> saveOrUpdateParkingLot(PublicParkingDataDto dto) {
        return Mono.fromCallable(() -> {
                    return parkingLotRepository.findByParkingLotNo(dto.getPrkplceNo())
                            .map(existing -> {
                                existing.updateInfo(dto);
                                return parkingLotRepository.save(existing);
                            })
                            .orElseGet(() -> {
                                ParkingLot newLot = ParkingLot.builder()
                                        .parkingLotNo(dto.getPrkplceNo())
                                        .name(dto.getPrkplceNm())
                                        .parkingLotSe(dto.getPrkplceSe())
                                        .parkingLotType(dto.getPrkplceType())
                                        .roadAddress(dto.getRdnmadr())
                                        .landAddress(dto.getLnmadr())
                                        .capacity(dto.getPrkcmprt())
                                        .feedingSe(dto.getFeedingSe())
                                        .enforceSe(dto.getEnforceSe())
                                        .operDay(dto.getOperDay())
                                        .weekdayOperOpen(dto.getWeekdayOperOpenHhmm())
                                        .weekdayOperClose(dto.getWeekdayOperColseHhmm())
                                        .satOperOpen(dto.getSatOperOperOpenHhmm())
                                        .satOperClose(dto.getSatOperCloseHhmm())
                                        .holidayOperOpen(dto.getHolidayOperOpenHhmm())
                                        .holidayOperClose(dto.getHolidayCloseOpenHhmm())
                                        .parkingChargeInfo(dto.getParkingchrgeInfo())
                                        .basicTime(dto.getBasicTime())
                                        .basicCharge(dto.getBasicCharge())
                                        .addUnitTime(dto.getAddUnitTime())
                                        .addUnitCharge(dto.getAddUnitCharge())
                                        .dayTicketAdjTime(dto.getDayCmmtktAdjTime())
                                        .dayTicketCharge(dto.getDayCmmtkt())
                                        .monthTicketCharge(dto.getMonthCmmtkt())
                                        .paymentMethod(dto.getMetpay())
                                        .spcmnt(dto.getSpcmnt())
                                        .institutionNm(dto.getInstitutionNm())
                                        .phoneNumber(dto.getPhoneNumber())
                                        .pwdbsPpkZoneYn(dto.getPwdbsPpkZoneYn())
                                        .latitude(dto.getLatitude())
                                        .longitude(dto.getLongitude())
                                        .referenceDate(dto.getReferenceDate())
                                        .insttCode(dto.getInsttCode())
                                        .insttNm(dto.getInsttNm())
                                        .realtimeParkingCode(null)
                                        .isRealtimeSupported(false)
                                        .build();
                                return parkingLotRepository.save(newLot);
                            });
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.warn("주차장 데이터 저장 중 오류 발생 (무시하고 계속 진행) - 주차장번호: {}, 사유: {}", dto.getPrkplceNo(), e.getMessage());
                    return Mono.empty();
                });
    }
}