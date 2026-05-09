package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.faq.repository.CodeFaqRepository;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeFaqInitializer {

    private final CodeFaqRepository codeFaqRepository;
    private final DataSource dataSource;

    @PostConstruct
    public void initialize() {
        // 첫 부팅에는 ddl-auto=none 으로 테이블이 아직 없을 수 있어 count 호출이 실패할 수 있음 → 0 으로 간주.
        long existing;
        try {
            existing = codeFaqRepository.count();
        } catch (Exception e) {
            existing = 0;
        }
        if (existing > 0) return;

        ClassPathResource resource = new ClassPathResource("sql/code_faq.sql");

        if (!resource.exists()) return;

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
