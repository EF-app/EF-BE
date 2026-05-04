package com.nokcha.efbe.domain.log.service;

import com.nokcha.efbe.domain.log.entity.LoginFailureReason;
import com.nokcha.efbe.domain.log.entity.LoginPlatform;
import com.nokcha.efbe.domain.log.entity.UserLoginLog;
import com.nokcha.efbe.domain.log.repository.UserLoginLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserLoginLogService {

    private final UserLoginLogRepository userLoginLogRepository;

    // 로그인 성공 이력 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(Long userId, String loginIdAttempt, HttpServletRequest request, String deviceId, LoginPlatform platform, boolean isScodeStep) {
        userLoginLogRepository.save(UserLoginLog.builder()
                .userId(userId)
                .loginIdAttempt(loginIdAttempt)
                .loginAt(LocalDateTime.now())
                .ipAddress(resolveIpAddress(request))
                .deviceId(deviceId)
                .platform(resolvePlatform(platform))
                .isSuccess(true)
                .failureReason(null)
                .isScodeStep(isScodeStep)
                .build());
    }

    // 로그인 실패 이력 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(Long userId, String loginIdAttempt, HttpServletRequest request, String deviceId, LoginPlatform platform, LoginFailureReason failureReason, boolean isScodeStep) {
        userLoginLogRepository.save(UserLoginLog.builder()
                .userId(userId)
                .loginIdAttempt(loginIdAttempt)
                .loginAt(LocalDateTime.now())
                .ipAddress(resolveIpAddress(request))
                .deviceId(deviceId)
                .platform(resolvePlatform(platform))
                .isSuccess(false)
                .failureReason(failureReason)
                .isScodeStep(isScodeStep)
                .build());
    }

    // 요청에서 클라이언트 IP 추출
    private String resolveIpAddress(HttpServletRequest request) {
        if (request == null) return null;

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    // null 플랫폼은 UNKNOWN으로 정규화
    private LoginPlatform resolvePlatform(LoginPlatform platform) {
        return platform == null ? LoginPlatform.UNKNOWN : platform;
    }
}
