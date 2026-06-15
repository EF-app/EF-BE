package com.nokcha.efbe.domain.match.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.match.dto.request.MutualToggleReqDto;
import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.domain.match.dto.response.MatchFeedActionResultRspDto;
import com.nokcha.efbe.domain.match.dto.response.MatchLikesCountRspDto;
import com.nokcha.efbe.domain.match.dto.response.MutualMatchItemRspDto;
import com.nokcha.efbe.domain.match.dto.response.ReceivedLikeItemRspDto;
import com.nokcha.efbe.domain.match.dto.response.SentLikeItemRspDto;
import com.nokcha.efbe.domain.match.service.MatchListActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 매칭 목록 (받은/누른/서로 좋아요) 화면 — 카운트 + cursor list + 액션.
 *  하나의 service (MatchListActionService) 로 통합. 액션은 매칭피드 service (recordAction) 와 재사용.
 */
@Tag(name = "Match List Action", description = "매칭 목록 (받은/누른/서로 좋아요) 카운트/목록/액션")
@RestController
@RequestMapping("/v1/users/me/matches")
@RequiredArgsConstructor
public class MatchListActionController {

    private final MatchListActionService matchListActionService;
    private final SecurityUtil securityUtil;

    /* ─────────────────────────── 카운트 ─────────────────────────── */

    @Operation(summary = "내 좋아요 카운트 (sent/received/mutual)",
            description = "my 화면 LikesBanner 의 3개 숫자. action_type IN (LIKE, SUPER_LIKE), 7일 cutoff")
    @GetMapping("/count")
    public RspTemplate<MatchLikesCountRspDto> getCount() {
        Long meId = securityUtil.getCurrentUserId();
        return new RspTemplate<>(HttpStatus.OK, "내 좋아요 카운트 조회 성공",
                matchListActionService.getCount(meId));
    }

    /* ─────────────────────────── 보낸 좋아요 ─────────────────────────── */

    @Operation(summary = "보낸 좋아요 목록 (i-like, cursor)",
            description = "보낸 좋아요 화면. action_type IN (LIKE, SUPER_LIKE), 7일 cutoff, " +
                    "mutual 제외 (양쪽 LIKE row 가 다 있는 페어는 we-like 화면 소관). 정렬 ma.id DESC. " +
                    "isSuper = SUPER_LIKE AND create_time >= NOW - 3일.")
    @GetMapping("/i-like")
    public RspTemplate<CursorPageResponse<SentLikeItemRspDto>> getSent(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long meId = securityUtil.getCurrentUserId();
        return new RspTemplate<>(HttpStatus.OK, "보낸 좋아요 목록 조회 성공",
                matchListActionService.getSent(meId, cursor, size));
    }

    @Operation(summary = "보낸 좋아요 취소",
            description = "보낸 좋아요 카드의 X 버튼. actor=me, target=otherUserId 의 " +
                    "LIKE/SUPER_LIKE row 를 PASS 로 UPDATE (action_type=PASS, expires_at=NOW+cooldown, tags_json=NULL) " +
                    "SUPER_LIKE 도 PASS 변환 — 별 환불 X ")
    @PatchMapping("/likes/sent/{otherUserId}")
    public RspTemplate<Void> cancelSent(@PathVariable Long otherUserId) {
        Long meId = securityUtil.getCurrentUserId();
        matchListActionService.cancelSentLike(meId, otherUserId);
        return new RspTemplate<>(HttpStatus.OK, "보낸 좋아요를 취소했습니다.");
    }

    /* ─────────────────────────── 받은 좋아요 ─────────────────────────── */

    @Operation(summary = "받은 좋아요 목록 (u-like, cursor)",
            description = "받은 좋아요 화면. action_type IN (LIKE, SUPER_LIKE), 7일 cutoff, " +
                    "미응답만 표시 (NOT EXISTS my LIKE/PASS row — mutual 은 we-like, 처리 완료는 자동 사라짐). " +
                    "정렬 ma.id DESC. isSuper = SUPER_LIKE AND create_time >= NOW - 3일.")
    @GetMapping("/u-like")
    public RspTemplate<CursorPageResponse<ReceivedLikeItemRspDto>> getReceived(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long meId = securityUtil.getCurrentUserId();
        return new RspTemplate<>(HttpStatus.OK, "받은 좋아요 목록 조회 성공",
                matchListActionService.getReceived(meId, cursor, size));
    }

    @Operation(summary = "받은 좋아요 ❤ — 수락 (mutual 성사)",
            description = "내 LIKE row INSERT (actor=me, target=other). " +
                    "받은좋아요는 정의상 상대가 이미 LIKE row 를 가지고 있으므로 mutual 자동 성사 (match_results 기록) " +
                    "매칭피드와 달리 popup/라우팅 없음 — 카드가 화면 안에서 mutual 로 변환 (채팅 아이콘) 됨 " +
                    "응답 isMatched/chatRoomId 는 FE 옵티미스틱 변환·채팅 아이콘 라우팅에 사용. " +
                    "MatchFeedActionService.recordAction(LIKE) 재사용 (단방향 의존).")
    @PatchMapping("/likes/received/{otherUserId}/accept")
    public RspTemplate<MatchFeedActionResultRspDto> acceptReceived(@PathVariable Long otherUserId) {
        Long meId = securityUtil.getCurrentUserId();
        MatchFeedActionResultRspDto result = matchListActionService.acceptReceivedLike(meId, otherUserId);
        return new RspTemplate<>(HttpStatus.OK, "받은 좋아요를 수락했습니다.", result);
    }

    @Operation(summary = "받은 좋아요 ✕ — 화면에서 제거",
            description = "내 PASS row INSERT (actor=me, target=other, expires_at=NOW+30d). " +
                    "받은좋아요 SQL 의 NOT EXISTS 내 PASS row 필터로 자동 사라짐")
    @PatchMapping("/likes/received/{otherUserId}/dismiss")
    public RspTemplate<Void> dismissReceived(@PathVariable Long otherUserId) {
        Long meId = securityUtil.getCurrentUserId();
        matchListActionService.dismissReceivedLike(meId, otherUserId);
        return new RspTemplate<>(HttpStatus.OK, "받은 좋아요를 제거했습니다.");
    }

    /* ─────────────────────────── 서로 좋아요 ─────────────────────────── */

    @Operation(summary = "서로 좋아요 목록 (we-like, cursor)",
            description = "서로 좋아요 화면. match_results 기반 (양쪽 LIKE/SUPER_LIKE), 7일 cutoff, " +
                    "isFresh=NOW-3h, isSuper=양쪽 중 SUPER_LIKE 하나라도. 정렬은 match_results.create_time DESC 고정 — cursor 무한 스크롤.")
    @GetMapping("/we-like")
    public RspTemplate<CursorPageResponse<MutualMatchItemRspDto>> getMutual(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long meId = securityUtil.getCurrentUserId();
        return new RspTemplate<>(HttpStatus.OK, "서로 좋아요 목록 조회 성공",
                matchListActionService.getMutual(meId, cursor, size));
    }

    @Operation(summary = "서로 좋아요 카드 하트 토글",
            description = "body { action: CANCEL | RESTORE }. CANCEL=내 LIKE/SUPER_LIKE → PASS UPDATE (mutual 해제). " +
                    "RESTORE=내 PASS → LIKE UPDATE + tags_json 재계산 (SUPER_LIKE 원본은 LIKE 로 단순화). " +
                    "이미 같은 상태면 idempotent 성공")
    @PatchMapping("/likes/mutual/{otherUserId}")
    public RspTemplate<Void> toggleMutual(
            @PathVariable Long otherUserId,
            @Valid @RequestBody MutualToggleReqDto body) {
        Long meId = securityUtil.getCurrentUserId();
        matchListActionService.toggleMutual(meId, otherUserId, body.action());
        return new RspTemplate<>(HttpStatus.OK,
                body.action() == MutualToggleReqDto.Action.CANCEL ? "매칭을 끊었습니다." : "매칭을 복구했습니다.");
    }
}
