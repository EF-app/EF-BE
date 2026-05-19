package com.nokcha.efbe.domain.balGame.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.balGame.dto.request.BalVoteReqDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalVoteRspDto;
import com.nokcha.efbe.domain.balGame.service.BalVoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 밸런스 게임 투표 RESTful 컨트롤러
@Tag(name = "BalGame Vote", description = "밸런스 게임 투표")
@RestController
@RequestMapping("/v1/bal-game/{gameUuid}/votes")
@RequiredArgsConstructor
public class BalVoteController {

    private final BalVoteService balVoteService;
    private final SecurityUtil securityUtil;

    // 신규 투표 — 외부 노출 식별자(gameUuid)
    @Operation(summary = "신규 투표")
    @PostMapping
    public ResponseEntity<RspTemplate<BalVoteRspDto>> createVote(@PathVariable String gameUuid,
                                                                 @Valid @RequestBody BalVoteReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        BalVoteRspDto data = balVoteService.createVote(gameUuid, userId, req.getChoice());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "투표 성공", data));
    }

    // 투표 변경
    @Operation(summary = "투표 변경")
    @PutMapping
    public ResponseEntity<RspTemplate<BalVoteRspDto>> updateVote(@PathVariable String gameUuid,
                                                                 @Valid @RequestBody BalVoteReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        BalVoteRspDto data = balVoteService.updateVote(gameUuid, userId, req.getChoice());
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "투표 변경 성공", data));
    }

    // 내 투표 결과 조회 (% 포함)
    @Operation(summary = "내 투표 결과 조회", description = "% 포함")
    @GetMapping("/me")
    public ResponseEntity<RspTemplate<BalVoteRspDto>> getMyVote(@PathVariable String gameUuid) {
        Long userId = securityUtil.getCurrentUserId();
        BalVoteRspDto data = balVoteService.getMyVote(gameUuid, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "내 투표 조회 성공", data));
    }
}
