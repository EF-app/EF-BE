package com.nokcha.efbe.common.init;

import com.nokcha.efbe.domain.profile.entity.Purpose;
import com.nokcha.efbe.domain.user.entity.BanStatus;
import com.nokcha.efbe.domain.user.entity.Job;
import com.nokcha.efbe.domain.user.entity.Role;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    public void initialize() {
        if (userRepository.existsByLoginId("admin")) return;

        userRepository.save(User.builder()
                .uuid("1")
                .loginId("admin")
                .password(passwordEncoder.encode(adminPassword))
                .phone("010")
                .scode("0000")
                .nickname("관리자")
                .birth(19000101)
                .isWithdraw(false)
                .areaId(1L)
                .purpose(Purpose.MIXED)
                .role(Role.ROLE_ADMIN)
                .banStatus(BanStatus.NONE)
                .build());
    }
}
