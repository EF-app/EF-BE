package com.nokcha.efbe.common.config;

import com.nokcha.efbe.common.auth.filter.AuthRateLimitFilter;
import com.nokcha.efbe.common.auth.filter.JwtAuthenticationFilter;
import com.nokcha.efbe.common.auth.handler.JwtAccessDeniedHandler;
import com.nokcha.efbe.common.auth.handler.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    // dev 는 와일드카드 패턴, prod 는 application-prod.yml 에서 화이트리스트 명시.
    @Value("${cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*,http://192.168.*.*:*}")
    private List<String> allowedOriginPatterns;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 브라우저 CORS preflight 응답용 — RN native 앱은 적용 대상 아님(브라우저 정책),
    // Vite/Expo Web 빌드/Swagger UI 에서 필요.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(allowedOriginPatterns);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization", "Location"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, AuthRateLimitFilter authRateLimitFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v1/users/signup/**",
                                "/v1/users/login"
                        ).permitAll()
                        // 밸런스 게임 조회는 비로그인 허용 (홈 진입·피드·단건). myChoice 는 viewerId=null 처리.
                        // 투표/댓글/좋아요 등 mutating 은 GET 이 아니므로 자연히 인증 필요.
                        .requestMatchers(HttpMethod.GET, "/v1/bal-game/**").permitAll()
                        // 포스트잇 — /me 만 본인 인증 필수, 피드·단건 조회는 비로그인 허용 (홈 노출용).
                        // 더 구체적인 경로 매처를 먼저 두어 /me 가 와일드카드보다 우선 평가되도록 한다.
                        .requestMatchers(HttpMethod.GET, "/v1/post-it/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/post-it", "/v1/post-it/*").permitAll()
                        .anyRequest().authenticated())
                // permitAll 인증 엔드포인트 brute-force 방어 — JWT 필터보다 먼저 평가.
                .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
