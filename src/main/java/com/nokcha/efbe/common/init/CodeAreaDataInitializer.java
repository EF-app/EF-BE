package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.area.repository.AreaRepository;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeAreaDataInitializer {

    private final AreaRepository areaRepository;
    private final DataSource dataSource;

    @PostConstruct
    public void initialize() {
        if (areaRepository.count() > 0) return;

        ClassPathResource resource = new ClassPathResource("static/area.sql");

        if (!resource.exists()) return;

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
