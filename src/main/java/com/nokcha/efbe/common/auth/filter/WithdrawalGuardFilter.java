package com.nokcha.efbe.common.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.exception.dto.ErrorRspDto;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 탈퇴 대기(WITHDRAWING·30일 유예) 유저의 API 호출을 화이트리스트 외에는 모두 차단.
 *
 *  탈퇴 신청 시 계정은 "즉시 이용 정지" 되지만, 30일 내 철회가 가능해야 하므로 로그인 자체는 허용한다.
 *  로그인 후에는 이 필터가 철회·로그아웃·고객지원 외 모든 서비스 API 를 막아, 실질적 이용 정지를 강제한다.
 *
 *  화이트리스트:
 *   - 탈퇴 철회 (/v1/users/me/withdrawal/cancel) ← 핵심
 *   - 내 요약/제재조회 (/v1/users/me/summary, /v1/users/me/suspensions) — 대기 화면 표시용
 *   - 재로그인/토큰갱신/로그아웃
 *   - 고객지원 FAQ·약관·1:1 문의
 */
@Component
@RequiredArgsConstructor
public class WithdrawalGuardFilter extends OncePerRequestFilter {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private static final List<String> WHITELIST = List.of(
            "/v1/users/me/withdrawal/cancel",   // 철회 (핵심)
            "/v1/users/me/summary",
            "/v1/users/me/suspensions",
            "/v1/users/login",
            "/v1/users/token/refresh",
            "/v1/users/logout",
            "/v1/faq",
            "/v1/faq/**",
            "/v1/policies",
            "/v1/policies/**",
            "/v1/feedback",
            "/v1/feedback/**"
    );

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
        if (isAdmin) {
            filterChain.doFilter(request, response);
            return;
        }

        // 화이트리스트 경로는 탈퇴 상태와 무관하게 항상 허용 → DB 조회 없이 통과(핫패스 부하 절감)
        String path = request.getRequestURI();
        boolean whitelisted = WHITELIST.stream().anyMatch(pattern -> MATCHER.match(pattern, path));
        if (whitelisted) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = securityUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty() || !userOpt.get().isWithdrawing()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 탈퇴 대기 상태 + 비화이트리스트 경로 → 차단
        writeWithdrawingResponse(response);
    }

    private void writeWithdrawingResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorCode ec = ErrorCode.WITHDRAWN_USER;
        response.getWriter().write(objectMapper.writeValueAsString(
                new ErrorRspDto<>(ec.getCode(), HttpStatus.FORBIDDEN, ec.name(), ec.getMessage())
        ));
    }
}
