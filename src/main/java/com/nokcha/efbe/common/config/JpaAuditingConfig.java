package com.nokcha.efbe.common.config;

import com.nokcha.efbe.common.auth.model.AuthUserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class JpaAuditingConfig {

    // 작성자 정보
    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) return Optional.of(0L);

            if (authentication instanceof AnonymousAuthenticationToken) return Optional.of(0L);

            Object principal = authentication.getPrincipal();

            if (principal instanceof AuthUserPrincipal authUserPrincipal) {
                return Optional.ofNullable(authUserPrincipal.getUserId()).or(() -> Optional.of(0L));
            }

            return Optional.of(0L);
        };
    }
}
