package com.nokcha.efbe.common.config;

import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.core.LockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

// ShedLock 분산 락 — 다중 인스턴스 환경에서 스케줄러 중복 실행 방지.
// 스케줄러 메서드에 @SchedulerLock(name="...", lockAtMostFor="PT2M") 부여.
// 락 정보는 shedlock 테이블 (data.sql 에서 CREATE TABLE IF NOT EXISTS).
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT2M")
public class ShedLockConfig {

    @Bean
    public LockProvider lockProvider(JdbcTemplate jdbcTemplate) {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(jdbcTemplate)
                        .usingDbTime()
                        .build()
        );
    }
}
