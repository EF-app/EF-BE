package com.nokcha.efbe.domain.match.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.match.calculator.MatchCalculator;
import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.entity.MatchAction;
import com.nokcha.efbe.domain.match.model.MatchActionType;
import com.nokcha.efbe.domain.match.model.PairScore;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.repository.MatchActionRepository;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import com.nokcha.efbe.domain.match.tag.TagDisplayFormatter;
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
 *    - PASS         : expires_at = NOW() + cfg.passCooldownDays (기본 30일), tags_json = NULL
 *    - LIKE / SUPER_LIKE / POWER_MESSAGE : expires_at = NULL (영구 제외),
 *                                          tags_json = actor 관점 매칭 태그 freeze
 *    - 자기 자신 액션 금지 (CHECK chk_action_not_self 와 일관)
 *
 *  태그 freeze 정책 (명세서 §10.x — score_cache 폐기 + actions freeze):
 *    - 액션 시점에 MatchCalculator.score(actorCtx, targetCtx, cfg) 한 번 계산 + 저장
 *    - "내가 누른 좋아요" 화면 (actor 본인 조회) → tags_json 그대로 표시
 *    - "받은 좋아요"     화면 (target 조회)   → 표시 시점에 #내가/#나를 반전 헬퍼 적용 (별도 컨트롤러에서)
 *    - actor / target 중 한 명이 프로필 수정해도 액션 시점 freeze 가 유지됨 (stale 허용)
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
    private final UserManagement userMgmt;
    private final MatchCalculator matchCalculator;
    private final TagDisplayFormatter tagFormatter;

    @Transactional
    public void recordAction(long actorId, long targetId, MatchActionType type) {
        if (actorId == targetId) {
            throw new BusinessException(ErrorCode.MATCH_ACTION_SELF);
        }
        if (!userRepo.existsById(targetId)) {
            throw new BusinessException(ErrorCode.MATCH_ACTION_TARGET_NOT_FOUND);
        }

        MatchingConfig cfg = configLoader.load();

        /* DELETE + INSERT — UNIQUE(actor,target) 보장. */
        actionRepo.deleteByActorIdAndTargetId(actorId, targetId);
        actionRepo.flush();  // UNIQUE 충돌 방지: 같은 트랜잭션 내 DELETE→INSERT 강제 flush

        LocalDateTime expiresAt = type.hasCooldown()
                ? LocalDateTime.now().plusDays(cfg.getPassCooldownDays())
                : null;

        String tagsJson = (type == MatchActionType.PASS) ? null : computeTagsJson(actorId, targetId, cfg);

        actionRepo.save(MatchAction.builder()
                .actorId(actorId)
                .targetId(targetId)
                .actionType(type)
                .expiresAt(expiresAt)
                .tagsJson(tagsJson)
                .build());

        log.debug("[MatchAction] {} {} → {}, expiresAt={}, hasTags={}",
                type, actorId, targetId, expiresAt, tagsJson != null);
    }

    /**
     * actor 관점 매칭 태그 JSON 계산 — 액션 시점 freeze.
     *  실패해도 액션 자체는 성공시켜야 함 (태그는 부가 정보) — try/catch 로 null 폴백.
     */
    private String computeTagsJson(long actorId, long targetId, MatchingConfig cfg) {
        try {
            UserContext actorCtx  = userMgmt.loadContext(actorId);
            UserContext targetCtx = userMgmt.loadContext(targetId);
            if (actorCtx == null || targetCtx == null) {
                log.warn("[MatchAction] tags 계산 스킵 — 컨텍스트 누락, actor={}, target={}", actorId, targetId);
                return null;
            }
            PairScore ps = matchCalculator.score(actorCtx, targetCtx, cfg);
            return tagFormatter.renderJson(actorCtx, ps);
        } catch (Exception e) {
            log.warn("[MatchAction] tags 계산 실패 — actor={}, target={}, err={}",
                    actorId, targetId, e.getMessage(), e);
            return null;
        }
    }
}
