package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final AdminAccountRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    public void initialize() {
        if (adminRepository.existsByLoginId("admin")) return;

        adminRepository.save(AdminAccount.builder()
                .loginId("admin")
                .password(passwordEncoder.encode(adminPassword))
                .name("관리자")
                .isActive(true)
                .build());
    }
}
