package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.match.repository.MatchConfigRepository;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * code_match_config 시드 자동 적재.
 *  운영 시드 SQL ({@code sql/migration_match.sql}) 의 1번 섹션 (code_match_config) 만 재실행 시 안전하므로
 *  여기서는 전용 시드 파일 ({@code sql/code_match_config.sql}) 을 따로 두지 않고
 *  migration_match.sql 전체를 IF NOT EXISTS / INSERT 가드로 실행.
 *
 *  ※ migration_match.sql 의 CREATE 문은 모두 IF NOT EXISTS 라 멱등. INSERT 는 row 0 일 때만 진입.
 *  ※ test 프로파일에서는 비활성화 — 테스트는 운영 시드와 무관하게 동작.
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class CodeMatchConfigDataInitializer {

    private final MatchConfigRepository matchConfigRepository;
    private final DataSource dataSource;

    @PostConstruct
    public void initialize() {
        // 첫 부팅에는 테이블이 아직 없을 수 있어 count() 가 실패할 수 있음 → 0 으로 간주
        long existing;
        try {
            existing = matchConfigRepository.count();
        } catch (Exception e) {
            existing = 0;
        }
        if (existing > 0) return;

        ClassPathResource resource = new ClassPathResource("sql/migration_match.sql");
        if (!resource.exists()) return;

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
