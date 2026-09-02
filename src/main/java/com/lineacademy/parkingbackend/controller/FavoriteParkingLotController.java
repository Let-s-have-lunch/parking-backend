package com.lineacademy.parkingbackend.controller;

import com.lineacademy.parkingbackend.service.FavoriteParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteParkingLotController {

    private final FavoriteParkingLotService favoriteParkingLotService;

    @PostMapping("/{parkingLotId}")
    public Mono<ResponseEntity<Map<String, Object>>> toggleFavorite(
            Mono<Principal> principalMono,
            @PathVariable Long parkingLotId
    ) {
        return principalMono
                // Principal의 name에 통상적으로 email 식별자가 들어있다고 가정합니다.
                .map(Principal::getName)
                .flatMap(email -> favoriteParkingLotService.toggleFavorite(email, parkingLotId))
                .map(isAdded -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("isFavorite", isAdded);
                    response.put("message", isAdded ? "즐겨찾기에 추가되었습니다." : "즐겨찾기에서 해제되었습니다.");
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.status(401).build()); // 비로그인 접근 방어
    }

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getMyFavorites(Mono<Principal> principalMono) {
        return principalMono
                .map(Principal::getName)
                .flatMap(email -> favoriteParkingLotService.getMyFavorites(email).collectList())
                .map(lots -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", lots);
                    response.put("totalCount", lots.size());
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.status(401).build());
    }
}