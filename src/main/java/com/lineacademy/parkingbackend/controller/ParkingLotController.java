package com.lineacademy.parkingbackend.controller;

import com.lineacademy.parkingbackend.service.ParkingLotSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {
    private final ParkingLotSearchService parkingLotSearchService;

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getParkingLotsInBounds(
            @RequestParam BigDecimal swLat,
            @RequestParam BigDecimal neLat,
            @RequestParam BigDecimal swLng,
            @RequestParam BigDecimal neLng
    ) {
        return parkingLotSearchService.getParkingLotsInBounds(swLat, neLat, swLng, neLng)
                .collectList()
                .map(lots -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", lots);
                    response.put("totalCount", lots.size());

                    return ResponseEntity.ok(response);
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("주차장 목록 조회 중 오류 발생: ", e);

                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "주차장 정보를 불러오는 중 서버 오류가 발생했습니다.");

                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> getParkingLotDetail(
            @PathVariable Long id,
            Principal principal
    ) {
        String email = (principal != null) ? principal.getName() : null;


        return parkingLotSearchService.getParkingLotDetail(id, email)
                .map(lot -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", lot);
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(Exception.class, e -> {
                    log.error("주차장 상세 조회 중 오류 발생: ", e);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "서버 오류가 발생했습니다.");
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
                });
    }
}
