package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.balGame.repository.CodeNicknameWordRepository;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeNicknameWordInitializer {

    private final CodeNicknameWordRepository codeNicknameWordRepository;
    private final DataSource dataSource;

    @PostConstruct
    public void initialize() {
        long existing;
        try {
            existing = codeNicknameWordRepository.count();
        } catch (Exception e) {
            existing = 0;
        }
        if (existing > 0) return;

        ClassPathResource resource = new ClassPathResource("sql/code_nickname_word.sql");
        if (!resource.exists()) return;

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
