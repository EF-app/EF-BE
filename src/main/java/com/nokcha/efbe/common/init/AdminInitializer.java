package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.admin.entity.Admin;
import com.nokcha.efbe.domain.admin.repository.AdminRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    public void initialize() {
        if (adminRepository.existsByLoginId("admin")) {
            return;
        }

        adminRepository.save(Admin.builder()
                .loginId("admin")
                .password(passwordEncoder.encode(adminPassword))
                .nickname("관리자")
                .build());
    }
}
