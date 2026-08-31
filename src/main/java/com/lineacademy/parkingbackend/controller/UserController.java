package com.lineacademy.parkingbackend.controller;

import com.lineacademy.parkingbackend.dto.user.response.AuthResponse;
import com.lineacademy.parkingbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 현재 로그인한 사용자의 정보 조회 (/users/me)
     * 프론트엔드의 restoreLogin() 및 getMe()에서 호출합니다.
     */
    @GetMapping("/me")
    public Mono<ResponseEntity<Map<String, Object>>> getMyInfo(@AuthenticationPrincipal String email) {
        if (email == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return userService.getCurrentUser(email)
                .map(AuthResponse::from)
                .map(authResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", authResponse);
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", e.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
                });
    }
}
