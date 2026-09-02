package com.lineacademy.parkingbackend.controller;

import com.lineacademy.parkingbackend.dto.user.request.LoginRequest;
import com.lineacademy.parkingbackend.dto.user.request.SignUpRequest;
import com.lineacademy.parkingbackend.dto.user.response.AuthResponse;
import com.lineacademy.parkingbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService authService;

    /**
     * 회원가입 API
     */
    @PostMapping("/signup")
    public Mono<ResponseEntity<Map<String, Object>>> signup(@Valid @RequestBody SignUpRequest request) { // 💡 Mono 제거
        return authService.signup(request.getEmail(), request.getPassword(), request.getNickname())
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", AuthResponse.from(user));

                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .onErrorResume(e -> { // 💡 모든 예외를 잡아서 원인 메시지를 반환하도록 변경
                    log.error("회원가입 실패 원인: ", e);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", e.getMessage());

                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }

    /**
     * 로그인 API
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) { // 💡 Mono 제거
        return authService.login(request.getEmail(), request.getPassword())
                .map(tuple -> {
                    String token = tuple.getT1();
                    AuthResponse authResponse = AuthResponse.from(tuple.getT2());

                    Map<String, Object> data = new HashMap<>();
                    data.put("token", token);
                    data.put("user", authResponse);

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", data);

                    return ResponseEntity.ok().body(response);
                })
                .onErrorResume(e -> { // 💡 모든 예외를 잡아서 원인 메시지를 반환하도록 변경
                    log.error("로그인 실패 원인: ", e);
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", e.getMessage());

                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
                });
    }
}