package com.nokcha.efbe.domain.admin.account.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.account.dto.request.AdminAccountCreateReqDto;
import com.nokcha.efbe.domain.admin.account.dto.request.AdminAccountUpdateReqDto;
import com.nokcha.efbe.domain.admin.account.dto.request.AdminPasswordResetReqDto;
import com.nokcha.efbe.domain.admin.account.dto.response.AdminAccountRspDto;
import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.admin.log.entity.AdminLoginFailureReason;
import com.nokcha.efbe.domain.admin.log.entity.AdminLoginLog;
import com.nokcha.efbe.domain.admin.log.repository.AdminLoginLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    // 잠금 정책 모니터링용 윈도우 — AdminLoginLogService 의 LOCKOUT_WINDOW 와 동일 (1시간).
    // 두 곳이 정책 상수를 공유하지 않아 향후 변경 시 함께 갱신 필요.
    private static final Duration LOCKOUT_WINDOW = Duration.ofHours(1);

    private final AdminAccountRepository adminAccountRepository;
    private final AdminLoginLogRepository adminLoginLogRepository;
    private final PasswordEncoder passwordEncoder;

    // 목록 — keyword(name/loginId/email LIKE) / isActive 동적 필터
    @Transactional(readOnly = true)
    public Page<AdminAccountRspDto> getAdmins(String keyword, Boolean isActive, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return adminAccountRepository.search(kw, isActive, pageable)
                .map(this::toRspDto);
    }

    // 단건 상세 — 마지막 로그인 정보(admin_login_log) 포함
    @Transactional(readOnly = true)
    public AdminAccountRspDto getAdmin(Long id) {
        AdminAccount admin = adminAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        return toRspDto(admin);
    }

    // 생성
    @Transactional
    public AdminAccountRspDto createAdmin(AdminAccountCreateReqDto req) {
        if (adminAccountRepository.existsByLoginId(req.getLoginId())) {
            throw new BusinessException(ErrorCode.ALREADY_USER);
        }

        AdminAccount admin = AdminAccount.builder()
                .loginId(req.getLoginId())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getName())
                .email(req.getEmail())
                .isActive(true)
                .build();

        AdminAccount saved = adminAccountRepository.save(admin);
        return toRspDto(saved);
    }

    // 수정 — email
    @Transactional
    public AdminAccountRspDto updateAdmin(Long id, AdminAccountUpdateReqDto req) {
        AdminAccount admin = adminAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        admin.updateBasicInfo(req.getEmail());

        if (req.getIsActive() != null) {
            if (req.getIsActive()) admin.activate();
            else admin.deactivate();
        }
        return toRspDto(admin);
    }

    // 비밀번호 강제 변경 — 현재 비밀번호 확인 없이 즉시 교체 (관리자가 다른 관리자 비번 리셋 가능)
    @Transactional
    public void forceChangePassword(Long id, AdminPasswordResetReqDto req) {
        AdminAccount admin = adminAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        if (passwordEncoder.matches(req.getNewPassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.SAME_AS_OLD_PASSWORD);
        }
        admin.changePassword(passwordEncoder.encode(req.getNewPassword()));
    }

    // 잠금 해제 — 비밀번호 실패 누적으로 lockedUntil 이 설정된 계정을 관리자가 즉시 해제.
    @Transactional
    public AdminAccountRspDto unlock(Long id) {
        AdminAccount admin = adminAccountRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        admin.unlock();
        return toRspDto(admin);
    }

    // 마지막 성공 로그인은 admin_login_log 최근 비번 실패 횟수도 같은 윈도우(1시간) 로 집계해 잠금 정책 모니터링에 노출.
    private AdminAccountRspDto toRspDto(AdminAccount admin) {
        AdminLoginLog lastLogin = adminLoginLogRepository
                .findFirstByAdminIdAndIsSuccessOrderByLoginAtDesc(admin.getId(), true)
                .orElse(null);
        long recentFailures = adminLoginLogRepository
                .countByAdminIdAndFailureReasonAndLoginAtAfter(
                        admin.getId(),
                        AdminLoginFailureReason.INVALID_PASSWORD,
                        LocalDateTime.now().minus(LOCKOUT_WINDOW));
        return AdminAccountRspDto.of(
                admin,
                lastLogin == null ? null : lastLogin.getLoginAt(),
                lastLogin == null ? null : lastLogin.getIpAddress(),
                recentFailures
        );
    }
}
