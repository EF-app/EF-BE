package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.policy.repository.CodePolicyDocumentRepository;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@DependsOn("adminInitializer")
public class CodePolicyDocumentInitializer {

    private final CodePolicyDocumentRepository codePolicyDocumentRepository;
    private final DataSource dataSource;

    @PostConstruct
    public void initialize() {
        // 첫 부팅에는 ddl-auto=none 으로 테이블이 아직 없을 수 있어 count 호출이 실패할 수 있음 → 0 으로 간주.
        long existing;
        try {
            existing = codePolicyDocumentRepository.count();
        } catch (Exception e) {
            existing = 0;
        }
        if (existing > 0) return;

        ClassPathResource resource = new ClassPathResource("sql/code_policy_documents.sql");

        if (!resource.exists()) return;

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
