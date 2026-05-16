package com.nokcha.efbe.domain.admin.auth.service;

import com.nokcha.efbe.domain.admin.auth.entity.AdminLoginFailureReason;
import com.nokcha.efbe.domain.admin.auth.entity.AdminLoginLog;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.admin.auth.repository.AdminLoginLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

// admin_login_log 적재 + 비밀번호 실패 누적 시 자동 잠금.
// 로그 INSERT 자체가 실패해도 로그인 흐름은 막지 않고 silent 처리 (ERROR 로그로 모니터링 알람 트리거).
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLoginLogService {

    // 1시간 내 비밀번호 실패 5회 → 1시간 잠금.
    private static final Duration LOCKOUT_WINDOW = Duration.ofHours(1);
    private static final long LOCKOUT_THRESHOLD = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofHours(1);

    private final AdminLoginLogRepository adminLoginLogRepository;
    private final AdminAccountRepository adminAccountRepository;
    // TODO(admin-audit 도메인 도입 시 활성화): private final AdminAuditLogService adminAuditLogService;

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

    // INVALID_ID / ACCOUNT_INACTIVE / ACCOUNT_LOCKED 등 잠금 분기가 필요 없는 실패.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String loginId, Long adminId, AdminLoginFailureReason reason,
                              String ip, String userAgent) {
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

    // 5회 누적이면 admin_account.locked_until 갱신
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

                    // TODO(admin-audit 도메인 도입 시 활성화): audit_log 기록 — actor = 잠긴 admin 본인.
                    // adminAuditLogService.record(
                    //         adminId, "ADMIN_ACCOUNT_AUTO_LOCKED", "ADMIN_ACCOUNT", adminId,
                    //         null,
                    //         String.format("{\"lockedUntil\":\"%s\",\"trigger\":\"AUTO_5_FAILS_1H\",\"recentFailures\":%d}",
                    //                 until, recentFailures),
                    //         ip, userAgent
                    // );
                });
            }
        } catch (Exception e) {
            log.error("admin_login_log INSERT/lock 실패. loginId={}, adminId={}, ip={}",
                    loginId, adminId, ip, e);
        }
    }
}
