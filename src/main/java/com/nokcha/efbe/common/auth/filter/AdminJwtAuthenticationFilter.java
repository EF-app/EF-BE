package com.nokcha.efbe.common.auth.filter;

import com.nokcha.efbe.common.auth.jwt.AdminJwtTokenProvider;
import com.nokcha.efbe.common.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// /v1/admin/auth/** 경로에 적용되는 JWT 필터.
@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final AdminJwtTokenProvider adminJwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            try {
                if (adminJwtTokenProvider.validateToken(token)
                        && adminJwtTokenProvider.isAccessToken(token)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext()
                            .setAuthentication(adminJwtTokenProvider.getAuthentication(token));
                }
            } catch (BusinessException e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
