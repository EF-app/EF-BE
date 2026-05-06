package com.nokcha.efbe.common.auth.filter;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.auth.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            try {
                if (jwtTokenProvider.validateToken(token) && jwtTokenProvider.isAccessToken(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(token));
                }
            } catch (BusinessException e) {
                log.warn("[JWT] 인증 실패: code={}, message={}", e.getCode(), e.getMessage());
                SecurityContextHolder.clearContext();
            } catch (Exception e) {
                log.warn("[JWT] 인증 중 예상치 못한 예외: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
