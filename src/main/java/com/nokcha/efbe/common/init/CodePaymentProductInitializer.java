package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.payment.repository.CodePaymentProductRepository;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodePaymentProductInitializer {

    private final CodePaymentProductRepository codePaymentProductRepository;
    private final DataSource dataSource;

    @PostConstruct
    public void initialize() {
        long existing;
        try {
            existing = codePaymentProductRepository.count();
        } catch (Exception e) {
            existing = 0;
        }
        if (existing > 0) return;

        ClassPathResource resource = new ClassPathResource("sql/code_payment_product.sql");
        if (!resource.exists()) return;

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(resource);
        populator.setContinueOnError(false);
        populator.execute(dataSource);
    }
}
