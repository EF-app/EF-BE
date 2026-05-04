package com.nokcha.efbe.common.config;

import com.nokcha.efbe.common.security.SecurityUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    // 작성자 정보 (SecurityContext의 현재 유저 ID, 미인증 시 시스템 작성자 0L)
    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> Optional.of(SecurityUtil.getCurrentUserIdOrSystem());
    }
}
