package com.nokcha.efbe.domain.user.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.user.dto.request.UserScodeReqDto;
import com.nokcha.efbe.domain.user.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Info", description = "회원 정보 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/me")
public class UserInfoController {

    private final UserInfoService userInfoService;

    @Operation(summary = "보안코드 설정/수정", description = "로그인한 회원의 보안코드를 설정하거나 수정합니다.")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/scode")
    public RspTemplate<Void> updateScode(@Valid @RequestBody UserScodeReqDto reqDto) {
        userInfoService.updateScode(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "보안코드 설정이 완료되었습니다.");
    }
}
