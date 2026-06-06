package com.nokcha.efbe.domain.match.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.model.MatchActionType;
import com.nokcha.efbe.domain.match.entity.MatchAction;
import com.nokcha.efbe.domain.match.repository.MatchActionRepository;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 매칭 액션 도메인 서비스 (명세서 §4.2 패스 쿨다운 + match_actions 통합 필터).
 *
 *  정책:
 *    - 한 페어(actor → target) 당 활성 액션 1 개 → 변경 시 DELETE + INSERT
 *    - PASS         : expires_at = NOW() + cfg.passCooldownDays (기본 30일)
 *    - LIKE / SUPER_LIKE / POWER_MESSAGE : expires_at = NULL (영구 제외)
 *    - 자기 자신 액션 금지 (CHECK chk_action_not_self 와 일관)
 *
 *  ※ 상호 LIKE 시 매칭 성사 (채팅방 생성 등) 후처리는 채팅 도메인 작업 — 이번 범위 외.
 *  ※ 별 차감 (SUPER_LIKE / POWER_MESSAGE) 도 결제 도메인 작업 — 이번 범위 외.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchActionService {

    private final MatchActionRepository actionRepo;
    private final UserRepository userRepo;
    private final MatchingConfigLoader configLoader;

    @Transactional
    public void recordAction(long actorId, long targetId, MatchActionType type) {
        if (actorId == targetId) {
            throw new BusinessException(ErrorCode.MATCH_ACTION_SELF);
        }
        if (!userRepo.existsById(targetId)) {
            throw new BusinessException(ErrorCode.MATCH_ACTION_TARGET_NOT_FOUND);
        }

        /* DELETE + INSERT — UNIQUE(actor,target) 보장. */
        actionRepo.deleteByActorIdAndTargetId(actorId, targetId);
        actionRepo.flush();  // UNIQUE 충돌 방지: 같은 트랜잭션 내 DELETE→INSERT 강제 flush

        LocalDateTime expiresAt = type.hasCooldown()
                ? LocalDateTime.now().plusDays(configLoader.load().getPassCooldownDays())
                : null;

        actionRepo.save(MatchAction.builder()
                .actorId(actorId)
                .targetId(targetId)
                .actionType(type)
                .expiresAt(expiresAt)
                .build());

        log.debug("[MatchAction] {} {} → {}, expiresAt={}", type, actorId, targetId, expiresAt);
    }
}
