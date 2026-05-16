package com.nokcha.efbe.domain.user.account.service;

import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 평문으로 저장된 기존 보안코드를 BCrypt 인코딩으로 일회성 전환.
// BCrypt 해시는 $2a$/$2b$/$2y$ 로 시작하므로 prefix 로 인코딩 여부 판별.
@Slf4j
@Component
@RequiredArgsConstructor
public class ScodePlaintextMigrator {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrate() {
        List<User> users = userRepository.findAll();
        int migrated = 0;

        for (User user : users) {
            String scode = user.getScode();
            if (scode == null || scode.isBlank() || isBcryptHash(scode)) {
                continue;
            }
            user.updateScode(passwordEncoder.encode(scode));
            migrated++;
        }

        if (migrated > 0) {
            log.info("[ScodePlaintextMigrator] 평문 보안코드 {}건 BCrypt 인코딩 완료", migrated);
        }
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
