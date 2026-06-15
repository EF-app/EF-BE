package com.nokcha.efbe.domain.match.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.dto.request.MutualToggleReqDto;
import com.nokcha.efbe.domain.match.dto.response.MatchFeedActionResultRspDto;
import com.nokcha.efbe.domain.match.dto.response.MatchLikesCountRspDto;
import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.domain.match.dto.response.MutualMatchItemRspDto;
import com.nokcha.efbe.domain.match.dto.response.ReceivedLikeItemRspDto;
import com.nokcha.efbe.domain.match.dto.response.SentLikeItemRspDto;
import com.nokcha.efbe.domain.match.entity.MatchAction;
import com.nokcha.efbe.domain.match.model.MatchActionType;
import com.nokcha.efbe.domain.match.model.MatchTriggerType;
import com.nokcha.efbe.domain.match.repository.MatchActionRepository;
import com.nokcha.efbe.domain.match.query.MatchListQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 매칭 목록 (받은/보낸/서로 좋아요) 화면 도메인 서비스.
 *
 *  endpoint: /v1/users/me/matches/{count,u-like,i-like,we-like,likes/received,likes/sent,likes/mutual}
 *           (MatchListActionController)
 *  메서드:
 *    - getCount        : sent/received/mutual 3개 카운트 (read-only)
 *    - getReceived     : 받은 좋아요 cursor list
 *    - getSent         : 보낸 좋아요 cursor list
 *    - getMutual       : 서로 좋아요 cursor list (match_results.create_time DESC 고정)
 *
 *    - acceptReceivedLike  : 받은 좋아요 ❤ — 내 LIKE row INSERT (mutual 자동 성사)
 *    - dismissReceivedLike : 받은 좋아요 ✕ — 내 PASS row INSERT
 *    - cancelSentLike      : 보낸 좋아요 ✕ — LIKE/SUPER_LIKE → PASS UPDATE
 *    - toggleMutual        : 서로 좋아요 하트 토글 — CANCEL/RESTORE
 *
 *  ※ acceptReceivedLike  는 MatchFeedActionService.recordAction(LIKE) 재사용 (단방향 의존).
 *  ※ dismissReceivedLike 는 MatchFeedActionService.recordAction(PASS) 재사용 (단방향 의존).
 *  ※ toggleMutual RESTORE 는 MatchFeedActionService.computeTagsJson 재사용 (package-private) + MatchResultService.recordMutual.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchListActionService {

    private final MatchListQueryService matchListQuery;
    private final MatchActionRepository actionRepo;
    private final MatchingConfigLoader configLoader;
    // dismissReceivedLike 가 매칭피드 PASS 와 동일 패턴 (recordAction(PASS) 호출). toggleMutual RESTORE 시 tagsJson 재계산.
    private final MatchFeedActionService matchFeedActionService;
    // toggleMutual RESTORE 시 match_results 재기록.
    private final MatchResultService matchResultService;

    /* ─────────────────────────── 카운트 ─────────────────────────── */

    @Transactional(readOnly = true)
    public MatchLikesCountRspDto getCount(long meId) {
        return new MatchLikesCountRspDto(
                matchListQuery.countSent(meId),
                matchListQuery.countReceived(meId),
                matchListQuery.countMutual(meId)
        );
    }

    /* ─────────────────────────── List ─────────────────────────── */

    @Transactional(readOnly = true)
    public CursorPageResponse<ReceivedLikeItemRspDto> getReceived(long meId, String cursor, int size) {
        Long cursorId = parseCursor(cursor);
        int safeSize = Math.max(1, Math.min(size, 50));
        return matchListQuery.searchReceived(meId, cursorId, safeSize);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<SentLikeItemRspDto> getSent(long meId, String cursor, int size) {
        Long cursorId = parseCursor(cursor);
        int safeSize = Math.max(1, Math.min(size, 50));
        return matchListQuery.searchSent(meId, cursorId, safeSize);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<MutualMatchItemRspDto> getMutual(long meId, String cursor, int size) {
        Long cursorId = parseCursor(cursor);
        int safeSize = Math.max(1, Math.min(size, 50));
        return matchListQuery.searchMutual(meId, cursorId, safeSize);
    }

    private static Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try { return Long.parseLong(cursor); }
        catch (NumberFormatException e) { return null; }
    }

    /* ─────────────────────────── 액션 ─────────────────────────── */

    /**
     * 받은 좋아요 ❤ — 내 LIKE row INSERT (mutual 자동 성사).
     *  매칭피드 LIKE 와 통일 정책 — MatchFeedActionService.recordAction 재사용.
     */
    @Transactional
    public MatchFeedActionResultRspDto acceptReceivedLike(long meId, long otherUserId) {
        if (meId == otherUserId) {
            throw new BusinessException(ErrorCode.MATCH_ACTION_SELF);
        }
        return matchFeedActionService.recordAction(meId, otherUserId, MatchActionType.LIKE);
    }

    /**
     * 받은 좋아요 ✕ — 내 PASS row INSERT (expires_at=NOW+30d).
     *  매칭피드 PASS 와 통일 정책 — MatchFeedActionService.recordAction 재사용.
     */
    @Transactional
    public void dismissReceivedLike(long meId, long otherUserId) {
        if (meId == otherUserId) {
            throw new BusinessException(ErrorCode.MATCH_ACTION_SELF);
        }
        matchFeedActionService.recordAction(meId, otherUserId, MatchActionType.PASS);
    }

    /**
     * 보낸 좋아요 취소 — actor=me, target=other LIKE/SUPER_LIKE → PASS in-place UPDATE.
     *  매칭 풀 제외 유지 (PASS 30일 쿨다운). 별 환불 X
     */
    @Transactional
    public void cancelSentLike(long meId, long otherUserId) {
        if (meId == otherUserId) {
            throw new BusinessException(ErrorCode.MATCH_ACTION_SELF);
        }
        MatchAction action = actionRepo.findByActorIdAndTargetId(meId, otherUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MATCH_ACTION));

        MatchActionType type = action.getActionType();
        if (type == MatchActionType.PASS) return;
        if (type != MatchActionType.LIKE && type != MatchActionType.SUPER_LIKE) {
            throw new BusinessException(ErrorCode.NOT_FOUND_MATCH_ACTION);
        }
        MatchingConfig cfg = configLoader.load();
        action.changeToPass(LocalDateTime.now().plusDays(cfg.getPassCooldownDays()));
        log.debug("[MatchListAction] cancel sent {} → {}, type was {}", meId, otherUserId, type);
    }

    /**
     * 서로 좋아요 카드 하트 토글
     *  CANCEL  : 내 LIKE/SUPER_LIKE → PASS UPDATE (mutual 해제).
     *  RESTORE : 내 PASS → LIKE UPDATE + tags_json 재계산 + match_results 재기록.
     */
    @Transactional
    public void toggleMutual(long meId, long otherUserId, MutualToggleReqDto.Action action) {
        if (meId == otherUserId) {
            throw new BusinessException(ErrorCode.MATCH_ACTION_SELF);
        }
        MatchAction row = actionRepo.findByActorIdAndTargetId(meId, otherUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_MATCH_ACTION));

        MatchActionType type = row.getActionType();
        switch (action) {
            case CANCEL -> {
                if (type == MatchActionType.PASS) return;
                if (type != MatchActionType.LIKE && type != MatchActionType.SUPER_LIKE) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_MATCH_ACTION);
                }
                MatchingConfig cfg = configLoader.load();
                row.changeToPass(LocalDateTime.now().plusDays(cfg.getPassCooldownDays()));
            }
            case RESTORE -> {
                if (type == MatchActionType.LIKE || type == MatchActionType.SUPER_LIKE) return;
                if (type != MatchActionType.PASS) {
                    throw new BusinessException(ErrorCode.NOT_FOUND_MATCH_ACTION);
                }
                MatchingConfig cfg = configLoader.load();
                // MatchFeedActionService 의 tagsJson 계산 헬퍼 재사용 (package-private).
                String tagsJson = matchFeedActionService.computeTagsJson(meId, otherUserId, cfg);
                row.changeToLike(tagsJson);
                // 상대 row 가 LIKE/SUPER_LIKE 면 match_results 재기록 (idempotent).
                actionRepo.findByActorIdAndTargetId(otherUserId, meId).ifPresent(other -> {
                    MatchActionType ot = other.getActionType();
                    if (ot == MatchActionType.LIKE || ot == MatchActionType.SUPER_LIKE) {
                        matchResultService.recordMutual(meId, otherUserId,
                                ot == MatchActionType.SUPER_LIKE,
                                MatchTriggerType.MUTUAL_LIKE);
                    }
                });
            }
        }
        log.debug("[MatchListAction] mutual toggle {} {} → {} (was {})", action, meId, otherUserId, type);
    }
}
