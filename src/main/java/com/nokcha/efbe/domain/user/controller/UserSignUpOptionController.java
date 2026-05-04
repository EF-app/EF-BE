package com.nokcha.efbe.domain.user.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.user.dto.response.SignUpOptionGroupRspDto;
import com.nokcha.efbe.domain.user.service.UserSignUpOptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User SignUp Options", description = "회원가입 옵션 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/signup/options")
public class UserSignUpOptionController {

    private final UserSignUpOptionService userSignUpOptionService;

    @Operation(summary = "관심사 전체 항목 조회", description = "관심사 화면 렌더링용 전체 항목을 대분류별로 조회합니다.")
    @GetMapping("/interests")
    public RspTemplate<List<SignUpOptionGroupRspDto>> getInterestOptions() {
        return new RspTemplate<>(HttpStatus.OK, "관심사 전체 항목 조회가 완료되었습니다.", userSignUpOptionService.getInterestOptions());
    }

    @Operation(summary = "생활 습관 전체 항목 조회", description = "생활 습관 화면 렌더링용 전체 항목을 카테고리별로 조회합니다.")
    @GetMapping("/lifestyles")
    public RspTemplate<List<SignUpOptionGroupRspDto>> getLifestyleOptions() {
        return new RspTemplate<>(HttpStatus.OK, "생활 습관 전체 항목 조회가 완료되었습니다.", userSignUpOptionService.getLifestyleOptions());
    }

    @Operation(summary = "나에 대해서 전체 항목 조회", description = "나에 대해서 화면 렌더링용 전체 항목을 카테고리별로 조회합니다.")
    @GetMapping("/about-me")
    public RspTemplate<List<SignUpOptionGroupRspDto>> getAboutMeOptions() {
        return new RspTemplate<>(HttpStatus.OK, "나에 대해서 전체 항목 조회가 완료되었습니다.", userSignUpOptionService.getAboutMeOptions());
    }

    @Operation(summary = "이상형 설정 전체 항목 조회", description = "이상형 설정 화면 렌더링용 전체 항목을 카테고리별로 조회합니다.")
    @GetMapping("/ideal")
    public RspTemplate<List<SignUpOptionGroupRspDto>> getIdealOptions() {
        return new RspTemplate<>(HttpStatus.OK, "이상형 설정 전체 항목 조회가 완료되었습니다.", userSignUpOptionService.getIdealOptions());
    }
}
