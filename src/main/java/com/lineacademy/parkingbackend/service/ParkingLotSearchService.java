package com.lineacademy.parkingbackend.service;

import com.lineacademy.parkingbackend.domain.entity.ParkingLot;
import com.lineacademy.parkingbackend.dto.parking.response.ParkingLotResponse;
import com.lineacademy.parkingbackend.repository.FavoriteParkingLotRepository;
import com.lineacademy.parkingbackend.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingLotSearchService {

    private final ParkingLotRepository parkingLotRepository;
    private final FavoriteParkingLotRepository favoriteRepository;
    private final RealtimeParkingService seoulRealtimeParkingService;

    /**
     * [지도 드래그 시 호출]
     * 화면 영역 내 주차장 목록 조회 (실시간 데이터 제외, 극강의 속도)
     */
    @Transactional(readOnly = true)
    public Flux<ParkingLotResponse> getParkingLotsInBounds(
            BigDecimal swLat, BigDecimal neLat,
            BigDecimal swLng, BigDecimal neLng
    ) {
        return Mono.fromCallable(() ->
                        parkingLotRepository.findByLatitudeBetweenAndLongitudeBetween(swLat, neLat, swLng, neLng)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(lot -> ParkingLotResponse.of(lot, null));
    }

    /**
     * [마커 클릭 시 호출]
     * 주차장 단건 상세 조회 (캐시에서 실시간 데이터 병합)
     */
    @Transactional(readOnly = true)
    public Mono<ParkingLotResponse> getParkingLotDetail(Long id, String email) {
        return Mono.fromCallable(() -> {
                    // 1. 주차장 기본 정보 조회
                    ParkingLot lot = parkingLotRepository.findById(id).orElse(null);
                    if (lot == null) return null;

                    // 2. 로그인한 유저(이메일 있음)라면 즐겨찾기 여부 확인
                    boolean isFav = false;
                    if (email != null && !email.isEmpty()) {
                        isFav = favoriteRepository.existsByUserEmailAndParkingLotId(email, id);
                    }

                    // 리액티브 스트림으로 넘기기 위해 Object 배열로 묶어 반환
                    return new Object[]{lot, isFav};
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(data -> {
                    if (data == null) return Mono.empty();

                    ParkingLot lot = (ParkingLot) data[0];
                    boolean isFavorite = (Boolean) data[1];

                    // 3. 실시간 데이터 병합 후 isFavorite 상태와 함께 DTO 변환
                    if (lot.isRealtimeSupported() && lot.getRealtimeParkingCode() != null) {
                        return seoulRealtimeParkingService.getAvailableSpots(lot.getRealtimeParkingCode())
                                .map(spots -> ParkingLotResponse.of(lot, spots, isFavorite))
                                .defaultIfEmpty(ParkingLotResponse.of(lot, null, isFavorite));
                    }

                    return Mono.just(ParkingLotResponse.of(lot, null, isFavorite));
                });
    }
}
