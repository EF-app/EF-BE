package com.nokcha.efbe.domain.admin.auth.init;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.auth.entity.AdminRole;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// admin_account 가 비어있을 때만 admin01 시드. dev profile 한정.
// 운영(prod) 부팅 시에는 자동 비활성화 — 운영 admin 계정은 별도 발급 절차 필요.
@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class AdminAccountSeeder {

    private static final String SEED_LOGIN_ID = "admin01";
    private static final String SEED_PASSWORD = "1234";
    private static final String SEED_NAME = "관리자";

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (adminAccountRepository.findByLoginId(SEED_LOGIN_ID).isPresent()) return;

        adminAccountRepository.save(AdminAccount.builder()
                .uuid(UUID.randomUUID().toString())
                .loginId(SEED_LOGIN_ID)
                .password(passwordEncoder.encode(SEED_PASSWORD))
                .name(SEED_NAME)
                .role(AdminRole.ADMIN)
                .isActive(true)
                .build());

        log.info("[AdminAccountSeeder] {} seeded (dev profile).", SEED_LOGIN_ID);
    }
}
