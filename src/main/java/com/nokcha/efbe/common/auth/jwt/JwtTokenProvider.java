package com.nokcha.efbe.common.auth.jwt;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String USER_ID_CLAIM = "userId";
    private static final String LOGIN_ID_CLAIM = "loginId";
    private static final String ROLE_CLAIM = "role";
    private static final String SIGNUP_SESSION_ID_CLAIM = "signupSessionId";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REGISTRATION_TOKEN_TYPE = "REGISTRATION";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.registration-token-expiration}")
    private long registrationTokenExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String loginId, Role role) {
        Instant now = Instant.now();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .claim(USER_ID_CLAIM, userId)
                .claim(LOGIN_ID_CLAIM, loginId)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiration)))
                .signWith(secretKey)
                .compact();
    }

    // 회원가입 단계용 등록 토큰
    public String createRegistrationToken(Long signupSessionId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(signupSessionId))
                .claim(TOKEN_TYPE_CLAIM, REGISTRATION_TOKEN_TYPE)
                .claim(SIGNUP_SESSION_ID_CLAIM, signupSessionId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(registrationTokenExpiration)))
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        // principal 은 userId(Long) — SecurityUtil 이 Long instance 분기로 ID 를 꺼냄.
        // 이전에는 loginId(String) 을 넣어 Long.parseLong 이 NFE → null fallback(1L) 로 빠지는 버그가 있었음.
        Long userId = claims.get(USER_ID_CLAIM, Long.class);
        String role = claims.get(ROLE_CLAIM, String.class);

        return new UsernamePasswordAuthenticationToken(
                userId,
                token,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    public Long getSignupSessionId(String token) {
        return getClaims(token).get(SIGNUP_SESSION_ID_CLAIM, Long.class);
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(getClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public boolean isRegistrationToken(String token) {
        return REGISTRATION_TOKEN_TYPE.equals(getClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_REGISTRATION_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REGISTRATION_TOKEN);
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
