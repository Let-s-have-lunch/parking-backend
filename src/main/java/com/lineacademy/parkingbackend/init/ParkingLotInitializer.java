package com.lineacademy.parkingbackend.init;

import com.lineacademy.parkingbackend.service.PublicDataSyncService;
import com.lineacademy.parkingbackend.service.RealtimeParkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingLotInitializer {

    private final PublicDataSyncService publicDataSyncService;
    private final RealtimeParkingService realtimeParkingService; // 💡 매핑 서비스 주입

    /**
     * 스프링 부트 애플리케이션 구동이 완료된 후(ApplicationReadyEvent) 자동으로 실행됩니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeParkingLots() {
        log.info("=== 애플리케이션 구동 완료: 공영주차장 데이터 초기화 프로세스 시작 ===");

        publicDataSyncService.syncParkingLots()
                // 💡 1단계 완료 후 2단계(매핑) 순차 실행
                .then(Mono.fromRunnable(() -> {
                    log.info("=== 1단계: 공영주차장 정적 데이터 동기화 완료 ===");
                    log.info("=== 2단계: 서울시 실시간 주차장 매핑 시작 ===");

                    realtimeParkingService.mapSeoulParkingToDb();

                    log.info("=== 2단계: 서울시 실시간 주차장 매핑 완료 ===");
                }))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        // 성공 시
                        null,
                        // 에러 발생 시
                        error -> log.error("주차장 데이터 초기화 프로세스 중 오류 발생: ", error),
                        // 완료 시
                        () -> log.info("=== 모든 주차장 데이터 초기화 프로세스 종료 ===")
                );
    }
}
