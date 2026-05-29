package com.nokcha.efbe.domain.suspension.service;

import com.nokcha.efbe.domain.suspension.entity.SuspensionType;
import com.nokcha.efbe.domain.suspension.entity.UserSuspension;
import com.nokcha.efbe.domain.suspension.repository.UserSuspensionRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 제재 도메인 핵심 — 시스템(배치)/유저(본인 활성 조회) 가 공유하는 평가 로직.
 *
 *  부과/해제/검색 같은 관리자 행위는 {@link com.nokcha.efbe.domain.admin.suspension.service.AdminSuspensionService}
 *  에서 담당하며, 부과/해제 시 도메인 불변식(users.status 동기화) 을 위해 이 서비스를 의존.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SuspensionService {

    private final UserSuspensionRepository userSuspensionRepository;

    // 활성 제재 기반으로 users.status 를 update. 부과/해제/배치/정합성 검증에서 호출
    public void evaluateAndUpdateStatus(User user) {
        if (user.getStatus() == UserStatus.WITHDRAWING || user.getStatus() == UserStatus.WITHDRAWN) {
            return;
        }
        UserStatus next = evaluateUserStatus(user.getId());
        if (user.getStatus() != next) {
            user.changeStatus(next);
        }
    }

    // 활성 제재 (is_lifted=false AND ends_at > now) 중 가장 강한 등급으로 UserStatus 도출. */
    @Transactional(readOnly = true)
    public UserStatus evaluateUserStatus(Long userId) {
        Optional<UserSuspension> strongest = userSuspensionRepository
                .findStrongestActiveByUserId(userId, LocalDateTime.now());
        if (strongest.isEmpty()) return UserStatus.ACTIVE;

        return switch (strongest.get().getSuspensionType()) {
            case PERMANENT -> UserStatus.PERMANENT;
            case TEMPORARY -> UserStatus.TEMPORARY;
            case WARNING -> UserStatus.ACTIVE; // 경고는 status 미반영
        };
    }

    // 유저 본인의 현재 활성 차단 제재 1건 (TEMPORARY/PERMANENT 만, WARNING 제외)
    @Transactional(readOnly = true)
    public Optional<UserSuspension> findActiveBlockingSuspension(Long userId) {
        return userSuspensionRepository
                .findStrongestActiveByUserId(userId, LocalDateTime.now())
                .filter(s -> s.getSuspensionType() != SuspensionType.WARNING);
    }
}
