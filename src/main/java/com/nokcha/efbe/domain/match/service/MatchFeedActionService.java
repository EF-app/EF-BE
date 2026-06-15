package com.nokcha.efbe.domain.match.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.match.calculator.MatchCalculator;
import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.dto.response.MatchFeedActionResultRspDto;
import com.nokcha.efbe.domain.match.entity.MatchAction;
import com.nokcha.efbe.domain.match.model.MatchActionType;
import com.nokcha.efbe.domain.match.model.MatchTriggerType;
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
 * 매칭피드 (HI 탭) 카드 액션 도메인 서비스 (패스 쿨다운 + match_actions 통합 필터).
 *
 *  endpoint: POST/DELETE /v1/matches/{targetId}/actions (MatchFeedActionController)
 *  메서드:
 *    - recordAction : LIKE/PASS/SUPER_LIKE/POWER_MESSAGE 등록 (DELETE+INSERT). mutual 성사 시 MatchResultService 호출.
 *    - undoAction   : PASS row 되돌리기 (LIKE 류는 부수효과 때문에 금지).
 *
 *  정책:
 *    - 한 페어(actor → target) 당 활성 액션 1 개 → 변경 시 DELETE + INSERT
 *    - PASS         : expires_at = NOW() + cfg.passCooldownDays (기본 30일), tags_json = NULL
 *    - LIKE / SUPER_LIKE / POWER_MESSAGE : expires_at = NULL (영구 제외),
 *                                          tags_json = actor 관점 매칭 태그 freeze
 *    - 자기 자신 액션 금지
 *
 *  ※ MatchListActionService (받은/누른/서로 좋아요 화면 액션) 에서 recordAction 을 재사용 — 단방향 의존.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchFeedActionService {

    private final MatchActionRepository actionRepo;
    private final UserRepository userRepo;
    private final MatchingConfigLoader configLoader;
    private final UserManagement userMgmt;
    private final MatchCalculator matchCalculator;
    private final TagDisplayFormatter tagFormatter;
    // mutual 성사 시 match_results INSERT — recordAction 안에서 호출
    private final MatchResultService matchResultService;

    /**
     * 매칭피드 카드의 ❤/✕/⭐/💌 등록.
     *  - 같은 페어 기존 액션 자동 대체 (DELETE + INSERT)
     *  - LIKE/SUPER_LIKE 시 mutual 검사 → 양쪽이면 MatchResultService.recordMutual 호출
     *  - 응답 isMatched : 클라이언트 popup + 서로좋아요 라우팅 분기
     */
    @Transactional
    public MatchFeedActionResultRspDto recordAction(long actorId, long targetId, MatchActionType type) {
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

        // 양방향 매칭 성사 검사 — 내 액션이 LIKE/SUPER_LIKE 이고 상대도 LIKE/SUPER_LIKE 면 mutual.
        // mutual 성사 시 match_results INSERT (idempotent — UNIQUE 페어 보장).
        boolean isMatched = false;
        boolean mutualIsSuper = false;
        if (type == MatchActionType.LIKE || type == MatchActionType.SUPER_LIKE) {
            var otherActionOpt = actionRepo.findByActorIdAndTargetId(targetId, actorId);
            if (otherActionOpt.isPresent()) {
                var other = otherActionOpt.get();
                MatchActionType otherType = other.getActionType();
                if (otherType == MatchActionType.LIKE || otherType == MatchActionType.SUPER_LIKE) {
                    isMatched = true;
                    mutualIsSuper = type == MatchActionType.SUPER_LIKE
                            || otherType == MatchActionType.SUPER_LIKE;
                    matchResultService.recordMutual(actorId, targetId, mutualIsSuper,
                            MatchTriggerType.MUTUAL_LIKE);
                }
            }
        }

        log.debug("[MatchFeedAction] {} {} → {}, expiresAt={}, hasTags={}, isMatched={}, isSuper={}",
                type, actorId, targetId, expiresAt, tagsJson != null, isMatched, mutualIsSuper);

        // chatRoomId 는 chat 도메인 작업 후 추가. v1 은 null.
        return new MatchFeedActionResultRspDto(isMatched, null);
    }

    /**
     * 매칭피드 [되돌리기] — actor → target PASS row 1개 삭제.
     *  현 정책: PASS 만 허용. LIKE/SUPER_LIKE/POWER_MESSAGE 는 매칭 popup / 별 차감 / 채팅방 등
     *  부수 효과가 있어 단순 DELETE 불가.
     *  row 없으면 NOT_FOUND_MATCH_ACTION (idempotent 호출 대비).
     */
    @Transactional
    public void undoAction(long actorId, long targetId) {
        MatchAction action = actionRepo.findByActorIdAndTargetId(actorId, targetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MATCH_ACTION));
        if (action.getActionType() != MatchActionType.PASS) {
            throw new BusinessException(ErrorCode.MATCH_ACTION_UNDO_NOT_ALLOWED);
        }
        actionRepo.delete(action);
        log.debug("[MatchFeedAction] undo PASS {} → {}", actorId, targetId);
    }

    /**
     * actor 관점 매칭 태그 JSON 계산 — 액션 시점 freeze (LIKE 류만).
     *  실패해도 액션 자체는 성공시켜야 함 — try/catch 로 null 폴백.
     *
     *  ※ MatchListActionService.toggleMutual (RESTORE) 에서도 재계산 필요 — package-private 으로 노출.
     */
    String computeTagsJson(long actorId, long targetId, MatchingConfig cfg) {
        try {
            UserContext actorCtx  = userMgmt.loadContext(actorId);
            UserContext targetCtx = userMgmt.loadContext(targetId);
            if (actorCtx == null || targetCtx == null) {
                log.warn("[MatchFeedAction] tags 계산 스킵 — 컨텍스트 누락, actor={}, target={}", actorId, targetId);
                return null;
            }
            PairScore ps = matchCalculator.score(actorCtx, targetCtx, cfg);
            return tagFormatter.renderJson(actorCtx, ps);
        } catch (Exception e) {
            log.warn("[MatchFeedAction] tags 계산 실패 — actor={}, target={}, err={}",
                    actorId, targetId, e.getMessage(), e);
            return null;
        }
    }
}
