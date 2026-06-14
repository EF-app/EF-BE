package com.nokcha.efbe.common.config;

import com.nokcha.efbe.common.interceptor.UserActivityInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정 — Interceptor 등록 지점.
 *
 *  UserActivityInterceptor:
 *    모든 인증된 능동 활동에서 last_active_at 갱신. excludePathPatterns 로 정적/관리자/운영 제외.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserActivityInterceptor userActivityInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userActivityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 관리자 — admin 본인 활동이 매칭 풀에 영향 X
                        "/v1/admin/**",
                        // 본인 제재 조회 — 정지 user 가 활동중 표기되면 안 됨
                        "/v1/users/me/suspensions",
                        // heartbeat — endpoint 가 직접 Recorder 호출, 이중 갱신 방지
                        "/v1/users/me/heartbeat",
                        // 정적 공지/문서 — 비로그인 접근, 매칭 시그널 X
                        "/v1/notices/**",
                        "/v1/faq/**",
                        "/v1/policies/**",
                        // 운영 도구
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}
