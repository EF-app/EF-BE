package com.nokcha.efbe.common.auth.jwt;

import com.nokcha.efbe.common.auth.model.AuthAdminPrincipal;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
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

// 관리자용 JWT 발급·검증. 유저용 JwtTokenProvider 와 분리된 secret·수명을 가진다.
@Component
public class AdminJwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ADMIN_ID_CLAIM = "adminId";
    private static final String LOGIN_ID_CLAIM = "loginId";
    private static final String NAME_CLAIM = "name";
    private static final String ROLE_CLAIM = "role";
    private static final String ACCESS_TOKEN_TYPE = "ADMIN_ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "ADMIN_REFRESH";

    private static final String ROLE_PREFIX = "ROLE_";

    @Value("${jwt.admin-secret:${jwt.secret}}")
    private String secret;

    @Value("${jwt.admin-access-expiration:3600000}")
    private long accessTokenExpiration;

    @Value("${jwt.admin-refresh-expiration:28800000}")
    private long refreshTokenExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long adminId, String loginId, String name, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(adminId))
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .claim(ADMIN_ID_CLAIM, adminId)
                .claim(LOGIN_ID_CLAIM, loginId)
                .claim(NAME_CLAIM, name)
                .claim(ROLE_CLAIM, role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpiration)))
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(Long adminId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(adminId))
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .claim(ADMIN_ID_CLAIM, adminId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTokenExpiration)))
                .signWith(secretKey)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Long adminId = claims.get(ADMIN_ID_CLAIM, Long.class);
        String loginId = claims.get(LOGIN_ID_CLAIM, String.class);
        String name = claims.get(NAME_CLAIM, String.class);
        String role = claims.get(ROLE_CLAIM, String.class);

        AuthAdminPrincipal principal = new AuthAdminPrincipal(adminId, loginId, name, role);
        // 모든 admin role 을 단일 ROLE_ADMIN 으로 통일
        String authority = ROLE_PREFIX + "ADMIN";

        return new UsernamePasswordAuthenticationToken(
                principal,
                token,
                Collections.singletonList(new SimpleGrantedAuthority(authority))
        );
    }

    public Long getAdminId(String token) {
        return getClaims(token).get(ADMIN_ID_CLAIM, Long.class);
    }

    public boolean isAccessToken(String token) {
        return ACCESS_TOKEN_TYPE.equals(getClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH_TOKEN_TYPE.equals(getClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.ADMIN_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.ADMIN_TOKEN_INVALID);
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
