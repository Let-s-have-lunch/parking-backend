package com.lineacademy.parkingbackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

// 임포트 에러 방지를 위해서 만든 가짜 파일입니다.
// public~ 담당님. 이 파일 위에 내용 덮어 씌우면 됩니다.

@Slf4j
@Service
public class PublicDataSyncService {

    // 임시 목업 메서드 (빈 동작 후 완료 처리)
    public Mono<Void> syncParkingLots() {
        log.info("[Mock] 공공데이터 주차장 동기화 건너뜀 (목업)");
        return Mono.empty();
    }
}
