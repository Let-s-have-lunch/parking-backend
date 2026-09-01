package com.lineacademy.parkingbackend.service;

import com.lineacademy.parkingbackend.domain.entity.ParkingLot;
import com.lineacademy.parkingbackend.dto.parking.SeoulParkingDto;
import com.lineacademy.parkingbackend.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeParkingService {

    private final ParkingLotRepository parkingLotRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${realtime.api.key}")
    private String realtimeApiKey;

    private final String SEOUL_API_URL = "http://openapi.seoul.go.kr:8088/{apiKey}/json/GetParkingInfo/1/1000/";
    // ✨ 실시간 잔여면수를 저장할 인메모리 캐시 (Key: 서울시 주차장코드, Value: 잔여면수)
    private final Map<String, Integer> realtimeCache = new ConcurrentHashMap<>();

    /**
     * 1. 5분마다(300,000ms) 서울시 API를 호출하여 캐시를 갱신합니다.
     */
    @Scheduled(fixedRate = 300000)
    public void updateRealtimeCache() {
        log.info("서울시 실시간 주차장 현황 캐싱 시작...");

        webClientBuilder.build().get()
                .uri(SEOUL_API_URL, realtimeApiKey)
                .retrieve()
                .bodyToMono(SeoulParkingDto.class)
                .subscribe(response -> {
                    // 💡 바뀐 구조에 맞게 널 체크 및 데이터 접근
                    if (response != null && response.getGetParkingInfo() != null && response.getGetParkingInfo().getRow() != null) {
                        for (SeoulParkingDto.SeoulParkingData data : response.getGetParkingInfo().getRow()) {
                            if (data.getTpkct() != null && data.getNowPrkVhclCnt() != null) {
                                // 💡 Double로 받은 값을 int로 캐스팅하여 계산
                                int tpkct = data.getTpkct().intValue();
                                int nowCnt = data.getNowPrkVhclCnt().intValue();

                                int availableSpots = Math.max(0, tpkct - nowCnt);
                                realtimeCache.put(data.getPkltCd(), availableSpots);
                            }
                        }
                        log.info("서울시 실시간 주차장 현황 캐싱 완료 (총 {}건)", realtimeCache.size());
                    }
                }, error -> log.error("서울시 실시간 주차장 API 호출 실패: {}", error.getMessage()));
    }

    /**
     * 2. 프론트엔드 조회용: 캐시에서 즉시 잔여면수를 꺼내줍니다.
     */
    public Mono<Integer> getAvailableSpots(String realtimeParkingCode) {
        if (realtimeParkingCode == null || !realtimeCache.containsKey(realtimeParkingCode)) {
            return Mono.empty();
        }
        return Mono.just(realtimeCache.get(realtimeParkingCode));
    }

    /**
     * 3. 텍스트 정규화 기반 매핑 (초기 1회 또는 관리자 수동 실행)
     */
    @Transactional
    public void mapSeoulParkingToDb() {
        log.info("텍스트 기반 서울시 주차장 - 전국표준데이터 매핑 시작...");

        try {
            // 1. 서울시 데이터 전체 조회
            SeoulParkingDto response = webClientBuilder.build().get()
                    .uri(SEOUL_API_URL, realtimeApiKey)
                    .retrieve()
                    .bodyToMono(SeoulParkingDto.class)
                    .block();

            // 💡 바뀐 구조에 맞게 널 체크
            if (response == null || response.getGetParkingInfo() == null || response.getGetParkingInfo().getRow() == null) {
                log.warn("❌ 응답은 왔지만 row 필드를 파싱하지 못했습니다. (데이터 없음)");
                return;
            }

            // 💡 배열 데이터 꺼내기
            List<SeoulParkingDto.SeoulParkingData> rows = response.getGetParkingInfo().getRow();
            log.info("✅ 서울시 API에서 {}건의 주차장 데이터를 성공적으로 가져왔습니다.", rows.size());

            List<ParkingLot> dbParkingLots = parkingLotRepository.findAll();
            int matchCount = 0;

            // 💡 꺼낸 rows 배열을 반복
            for (SeoulParkingDto.SeoulParkingData seoulData : rows) {
                String seoulName = normalizeText(seoulData.getPkltNm());
                String seoulAddr = normalizeText(seoulData.getAddr());

                boolean isMatched = false;

                for (ParkingLot dbLot : dbParkingLots) {
                    if (dbLot.isRealtimeSupported()) continue;

                    String dbName = normalizeText(dbLot.getName());
                    String dbRoadAddr = normalizeText(dbLot.getRoadAddress());
                    String dbLandAddr = normalizeText(dbLot.getLandAddress());

                    // ✨ 매핑 조건 1: 이름이 비슷할 때 (포함 관계)
                    boolean nameMatch = !seoulName.isEmpty() && !dbName.isEmpty() &&
                            (dbName.contains(seoulName) || seoulName.contains(dbName));

                    // ✨ 매핑 조건 2: 주소가 비슷할 때 (도로명 또는 지번 중 하나라도 포함 관계면 통과)
                    // (기존의 equals보다 훨씬 유연하게 잡습니다)
                    boolean addrMatch = !seoulAddr.isEmpty() && (
                            (!dbRoadAddr.isEmpty() && (dbRoadAddr.contains(seoulAddr) || seoulAddr.contains(dbRoadAddr))) ||
                                    (!dbLandAddr.isEmpty() && (dbLandAddr.contains(seoulAddr) || seoulAddr.contains(dbLandAddr)))
                    );

                    // 이름이 맞거나, 주소가 맞으면 매핑 성공으로 간주
                    if (nameMatch || addrMatch) {
                        dbLot.updateRealtimeParkingCode(seoulData.getPkltCd());
                        dbLot.updateRealtimeSupported(true);

                        parkingLotRepository.save(dbLot);
                        matchCount++;
                        isMatched = true;
                        break;
                    }
                }

                // 만약 한 건도 매핑 안 된 서울시 주차장이 있다면 로그를 찍어봅니다. (원인 분석용)
                if (!isMatched) {
                    log.debug("매핑 실패 데이터: [API이름: {} / API주소: {}]", seoulData.getPkltNm(), seoulData.getAddr());
                }
            }
            log.info("🎉 주차장 매핑 완료! (총 {}건 중 {}건 성공)", rows.size(), matchCount);

        } catch (Exception e) {
            log.error("❌ 매핑 로직 수행 중 치명적인 에러 발생: ", e);
        }
    }

    /**
     * 💡 핵심 유틸: 문자열 비교를 위해 불필요한 단어와 공백을 모두 날립니다.
     */
    private String normalizeText(String text) {
        if (text == null) return "";
        return text.replaceAll("공영주차장|공영|주차장|노상|노외|\\(시\\)|\\(구\\)|\\(동\\)|서울특별시|서울시|번지|\\s", "");
    }
}
