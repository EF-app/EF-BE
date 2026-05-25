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
import org.springframework.web.bind.annotation.*;

@Tag(name = "BalGame Vote", description = "밸런스 게임 투표")
@RestController
@RequestMapping("/v1/bal-game/{gameId}/votes")
@RequiredArgsConstructor
public class BalVoteController {

    private final BalVoteService balVoteService;
    private final SecurityUtil securityUtil;

    // 신규 투표 — id 기반
    @Operation(summary = "신규 투표")
    @PostMapping
    public RspTemplate<BalVoteRspDto> createVote(@PathVariable Long gameId, @Valid @RequestBody BalVoteReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        BalVoteRspDto data = balVoteService.createVote(gameId, userId, req.getChoice());
        return new RspTemplate<>(HttpStatus.CREATED, "투표에 성공했습니다.", data);
    }

    // 투표 변경
    @Operation(summary = "투표 변경")
    @PutMapping
    public RspTemplate<BalVoteRspDto> updateVote(@PathVariable Long gameId, @Valid @RequestBody BalVoteReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        BalVoteRspDto data = balVoteService.updateVote(gameId, userId, req.getChoice());
        return new RspTemplate<>(HttpStatus.OK, "투표 변경 성공", data);
    }
}
