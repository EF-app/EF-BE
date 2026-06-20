package com.nokcha.efbe.common.init;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * code_match_config 시드 자동 적재.
 *  매 부팅 시 {@code sql/migration_match.sql} 전체 재실행.
 *  - CREATE TABLE 은 {@code IF NOT EXISTS} 로 멱등
 *  - INSERT 는 {@code INSERT IGNORE} 로 PK 충돌 무시 → 기존 row 보존, 빠진 row 자동 보충
 *
 *  ※ 운영 중 admin 이 실수로 row 1개 삭제 후 재기동해도 해당 row 가 자동 복구됨
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class CodeMatchConfigDataInitializer {

    private final DataSource dataSource;

    @PostConstruct
    public void initialize() {
        ClassPathResource resource = new ClassPathResource("sql/migration_match.sql");
        if (!resource.exists()) {
            log.warn("[CodeMatchConfig] migration_match.sql 파일 없음 — 시드 적재 skip");
            return;
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.setContinueOnError(false);
        populator.execute(dataSource);
        log.info("[CodeMatchConfig] 시드 SQL 실행 완료");
    }
}
