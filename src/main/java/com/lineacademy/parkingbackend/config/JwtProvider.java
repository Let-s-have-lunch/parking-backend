package com.lineacademy.parkingbackend.config;

import com.lineacademy.parkingbackend.domain.enums.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtProvider {

    // 최신 버전에서는 Key 대신 SecretKey 인터페이스 사용을 권장합니다.
    private final SecretKey key;
    private final long validityInMilliseconds;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long validityInMilliseconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.validityInMilliseconds = validityInMilliseconds;
    }

    // 토큰 생성 (ClaimsBuilder 대신 Jwts.builder()에서 바로 주입)
    public String createToken(String email, UserRole role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(email) // setSubject -> subject
                .claim("role", role.name()) // 추가 정보 세팅
                .issuedAt(now) // setIssuedAt -> issuedAt
                .expiration(validity) // setExpiration -> expiration
                .signWith(key) // 최신 버전은 알고리즘(HS256)을 키 크기를 보고 자동 추론합니다.
                .compact();
    }

    // 토큰에서 Authentication 객체 추출
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser() // parserBuilder() -> parser()
                .verifyWith(key) // setSigningKey() -> verifyWith()
                .build()
                .parseSignedClaims(token) // parseClaimsJws() -> parseSignedClaims()
                .getPayload(); // getBody() -> getPayload()

        String email = claims.getSubject();
        String roleStr = claims.get("role", String.class);

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + roleStr)
        );

        return new UsernamePasswordAuthenticationToken(email, token, authorities);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        } catch (JwtException e) {
            log.info("JWT 토큰 검증 중 오류가 발생했습니다: {}", e.getMessage());
        }
        return false;
    }
}
