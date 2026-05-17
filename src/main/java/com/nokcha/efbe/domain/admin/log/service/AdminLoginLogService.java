package com.nokcha.efbe.domain.admin.log.service;

import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.admin.log.entity.AdminLoginFailureReason;
import com.nokcha.efbe.domain.admin.log.entity.AdminLoginLog;
import com.nokcha.efbe.domain.admin.log.repository.AdminLoginLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLoginLogService {

    private static final Duration LOCKOUT_WINDOW = Duration.ofHours(1); // 1시간 내 비번 불일치 5회 시 1시간 잠금
    private static final long LOCKOUT_THRESHOLD = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofHours(1);

    private final AdminLoginLogRepository adminLoginLogRepository;
    private final AdminAccountRepository adminAccountRepository;
    // TODO: 관리자 감사 도메인 도입 시 활성화

    // 로그인 성공 기록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String loginId, Long adminId, String ip, String userAgent) {
        try {
            adminLoginLogRepository.save(AdminLoginLog.builder()
                    .loginIdAttempt(loginId)
                    .adminId(adminId)
                    .isSuccess(true)
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .loginAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("admin_login_log INSERT 실패 (success). loginId={}, adminId={}, ip={}",
                    loginId, adminId, ip, e);
        }
    }

    // 로그인 실패 기록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String loginId, Long adminId, AdminLoginFailureReason reason, String ip, String userAgent) {
        try {
            adminLoginLogRepository.save(AdminLoginLog.builder()
                    .loginIdAttempt(loginId)
                    .adminId(adminId)
                    .isSuccess(false)
                    .failureReason(reason)
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .loginAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("admin_login_log INSERT 실패 (failure). loginId={}, adminId={}, reason={}, ip={}",
                    loginId, adminId, reason, ip, e);
        }
    }

    // 비번 5회 불일치 시 잠금
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPasswordFailureAndLock(String loginId, Long adminId, String ip, String userAgent) {
        LocalDateTime now = LocalDateTime.now();
        try {
            adminLoginLogRepository.save(AdminLoginLog.builder()
                    .loginIdAttempt(loginId)
                    .adminId(adminId)
                    .isSuccess(false)
                    .failureReason(AdminLoginFailureReason.INVALID_PASSWORD)
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .loginAt(now)
                    .build());

            long recentFailures = adminLoginLogRepository
                    .countByAdminIdAndFailureReasonAndLoginAtAfter(
                            adminId, AdminLoginFailureReason.INVALID_PASSWORD,
                            now.minus(LOCKOUT_WINDOW));

            if (recentFailures >= LOCKOUT_THRESHOLD) {
                adminAccountRepository.findById(adminId).ifPresent(admin -> {
                    LocalDateTime until = now.plus(LOCKOUT_DURATION);
                    admin.lock(until);
                    log.warn("admin lockout triggered. adminId={}, loginId={}, recentFailures={}, until={}",
                            adminId, loginId, recentFailures, until);

                    // TODO: 관리자 감사 도메인 도입 시 활성화 - 감사 기록 — actor = 잠긴 admin 본인
                });
            }
        } catch (Exception e) {
            log.error("admin_login_log INSERT/lock 실패. loginId={}, adminId={}, ip={}",
                    loginId, adminId, ip, e);
        }
    }
}
