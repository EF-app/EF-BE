package com.nokcha.efbe.domain.block.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.block.dto.request.BlockCreateReqDto;
import com.nokcha.efbe.domain.block.dto.response.BlockRspDto;
import com.nokcha.efbe.domain.block.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.nokcha.efbe.domain.block.dto.response.BlockedUserRspDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 유저 차단 API — 포스트잇/채팅/매칭 화면 에서 유저가 다른 유저를 차단.
@Tag(name = "Block", description = "유저 차단 API")
@RestController
@RequestMapping("/v1/block")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "유저 차단",
            description = "로그인한 유저가 blockedUserId 유저를 차단. 자기 자신/중복 차단은 거부.")
    @PostMapping
    public ResponseEntity<RspTemplate<BlockRspDto>> createBlock(
            @Valid @RequestBody BlockCreateReqDto req
    ) {
        Long blockerId = securityUtil.getCurrentUserId();
        BlockRspDto data = blockService.createBlock(blockerId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "유저를 차단했습니다.", data));
    }

    @Operation(summary = "유저 차단 해제",
            description = "로그인한 유저가 blockedUserId 유저 차단을 해제. 차단 기록이 없으면 거부.")
    @DeleteMapping("/{blockedUserId}")
    public ResponseEntity<RspTemplate<Void>> unblock(@PathVariable Long blockedUserId) {
        Long blockerId = securityUtil.getCurrentUserId();
        blockService.unblock(blockerId, blockedUserId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "유저 차단을 해제했습니다."));
    }

    @Operation(summary = "내 차단 목록 조회",
            description = "로그인한 유저가 차단한 유저 목록 (최신순). 차단 대상의 닉네임/나이/지역/MBTI 포함.")
    @GetMapping
    public ResponseEntity<RspTemplate<List<BlockedUserRspDto>>> getMyBlocks() {
        Long blockerId = securityUtil.getCurrentUserId();
        List<BlockedUserRspDto> data = blockService.getMyBlocks(blockerId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "차단 목록을 조회했습니다.", data));
    }
}
