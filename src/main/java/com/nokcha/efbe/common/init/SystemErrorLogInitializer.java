package com.nokcha.efbe.common.init;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemErrorLogInitializer {

    private final DataSource dataSource;

    @PostConstruct
    public void initialize() {
        ClassPathResource resource = new ClassPathResource("sql/system_error_log.sql");
        if (!resource.exists()) return;

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
