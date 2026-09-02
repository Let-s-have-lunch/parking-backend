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
            Principal principal, // 💡 Mono 제거
            @PathVariable Long parkingLotId
    ) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(401).build());
        }

        String email = principal.getName();
        return favoriteParkingLotService.toggleFavorite(email, parkingLotId)
                .map(isAdded -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("isFavorite", isAdded);
                    response.put("message", isAdded ? "즐겨찾기에 추가되었습니다." : "즐겨찾기에서 해제되었습니다.");
                    return ResponseEntity.ok(response);
                });
    }

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> getMyFavorites(
            Principal principal // 💡 Mono 제거
    ) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(401).build());
        }

        String email = principal.getName();
        return favoriteParkingLotService.getMyFavorites(email).collectList()
                .map(lots -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", lots);
                    response.put("totalCount", lots.size());
                    return ResponseEntity.ok(response);
                });
    }
}