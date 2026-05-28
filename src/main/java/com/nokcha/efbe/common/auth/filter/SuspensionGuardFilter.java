package com.nokcha.efbe.common.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.exception.dto.ErrorRspDto;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 제재 상태(TEMPORARY/PERMANENT) 유저의 API 호출을 화이트리스트 외에는 모두 차단.
 *
 *  화이트리스트(차단 상태에서도 호출 허용):
 *   - 마이페이지 전체 (/v1/users/me/*) — 프로필 수정, 계정관리, 보안코드, 탈퇴, 제재조회
 *   - 차단한 사용자 (/v1/blocks*)
 *   - 고객지원 FAQ (/v1/faq*)
 *   - 고객지원 약관 (/v1/policies/*)
 *   - 고객지원 1:1 문의 (/v1/feedback*)
 *   - 로그아웃 (/v1/users/logout)
 *
 *  그 외 모든 API → 403 (errorCode=SUSPENDED_ACCESS_DENIED). FE 는 차단 화면 라우팅 유지.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuspensionGuardFilter extends OncePerRequestFilter {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** 제재 상태에서도 허용되는 API 경로 prefix. AntPathMatcher 패턴. */
    private static final List<String> WHITELIST = List.of(
            "/v1/users/me/**",          // 마이/계정/프로필수정/보안코드/탈퇴/제재조회
            "/v1/users/logout",         // 로그아웃
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

        // 비인증 (permitAll 경로) → 통과
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 관리자 → 통과
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
        if (isAdmin) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = securityUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            // 토큰 유효하나 user 가 없는 비정상 — 인증 단계에서 처리, 여기선 통과
            filterChain.doFilter(request, response);
            return;
        }

        UserStatus status = userOpt.get().getStatus();
        // 제재 상태 아니면 통과 (ACTIVE / WITHDRAWING / WITHDRAWN)
        // 단 WITHDRAWING/WITHDRAWN 은 로그인 단계에서 이미 막혔어야 함
        if (status != UserStatus.TEMPORARY && status != UserStatus.PERMANENT) {
            filterChain.doFilter(request, response);
            return;
        }

        // 제재 상태 — 화이트리스트 매칭
        String path = request.getRequestURI();
        boolean allowed = WHITELIST.stream().anyMatch(pattern -> MATCHER.match(pattern, path));
        if (allowed) {
            filterChain.doFilter(request, response);
            return;
        }

        // /v1/users/me/suspensions 한 번 호출해 채움 (이 경로는 화이트리스트라 통과).
        writeSuspendedResponse(response);
    }

    private void writeSuspendedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorCode ec = ErrorCode.SUSPENDED_ACCESS_DENIED;
        response.getWriter().write(objectMapper.writeValueAsString(
                new ErrorRspDto<>(ec.getCode(), HttpStatus.FORBIDDEN, ec.name(), ec.getMessage())
        ));
    }
}
