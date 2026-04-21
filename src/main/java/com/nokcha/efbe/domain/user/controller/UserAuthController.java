package com.nokcha.efbe.domain.user.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.user.dto.request.*;
import com.nokcha.efbe.domain.user.dto.request.*;
import com.nokcha.efbe.domain.user.dto.response.LoginRspDto;
import com.nokcha.efbe.domain.user.dto.response.SignUpCompleteRspDto;
import com.nokcha.efbe.domain.user.dto.response.SignUpProfileRspDto;
import com.nokcha.efbe.domain.user.dto.response.SignUpProgressRspDto;
import com.nokcha.efbe.domain.user.service.UserAuthService;
import com.nokcha.efbe.domain.user.service.UserSignUpProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "User Auth", description = "회원가입 및 로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final UserSignUpProfileService userSignUpProfileService;

    // 약관 동의
    @Operation(summary = "약관 동의", description = "약관 동의 여부를 저장하고 다음 단계용 회원가입 토큰을 발급합니다.")
    @PostMapping("/signup/terms")
    public RspTemplate<SignUpProgressRspDto> agreeTerms(@Valid @RequestBody TermsAgreementReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "약관 동의가 완료되었습니다.", userAuthService.agreeTerms(reqDto));
    }

    // 휴대폰 인증
    @Operation(summary = "휴대폰 인증", description = "성인 및 여성 인증이 완료된 휴대폰 정보를 저장합니다.")
    @PostMapping("/signup/phone-verification")
    public RspTemplate<SignUpProgressRspDto> verifyPhone(@Valid @RequestBody PhoneVerificationReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "휴대폰 인증이 완료되었습니다.", userAuthService.verifyPhone(reqDto));
    }

    // 아이디, 비밀번호 입력
    @Operation(summary = "아이디, 비밀번호 생성", description = "휴대폰 인증 완료 후 아이디와 비밀번호를 저장합니다.")
    @PostMapping("/signup/credentials")
    public RspTemplate<SignUpProgressRspDto> createCredentials(@Valid @RequestBody SignUpCredentialsReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "아이디와 비밀번호 입력이 완료되었습니다.", userAuthService.createCredentials(reqDto));
    }

    // 닉네임, 지역 입력
    @Operation(summary = "닉네임, 지역 입력", description = "닉네임과 지역 정보를 임시 회원가입 세션에 저장합니다.")
    @PostMapping("/signup/basic-info")
    public RspTemplate<SignUpProgressRspDto> createBasicInfo(@Valid @RequestBody SignUpBasicInfoReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "닉네임과 지역 입력이 완료되었습니다.", userAuthService.createBasicInfo(reqDto));
    }

    // 가입 목적 선택
    @Operation(summary = "가입 목적 선택", description = "닉네임과 지역 입력 완료 후 가입 목적을 저장합니다.")
    @PostMapping("/signup/purpose")
    public RspTemplate<SignUpProgressRspDto> createPurpose(@Valid @RequestBody SignUpPurposeReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "가입 목적 선택이 완료되었습니다.", userAuthService.createPurpose(reqDto));
    }

    // 프로필 등록
    @Operation(summary = "프로필 정보 등록", description = "관심사, 내 성향, 이상형, 한 줄 소개를 저장합니다.")
    @PostMapping("/signup/profile")
    public RspTemplate<SignUpProfileRspDto> createProfile(@Valid @RequestBody SignUpProfileReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "프로필 정보 등록이 완료되었습니다.", userSignUpProfileService.createProfile(reqDto));
    }

    // 프로필 이미지 등록
    @Operation(summary = "프로필 이미지 등록", description = "프로필 이미지를 업로드하고 회원가입 프로필 단계를 완료합니다.")
    @PostMapping(value = "/signup/profile/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RspTemplate<SignUpProfileRspDto> uploadProfileImages(
            @RequestPart("registrationToken") String registrationToken,
            @RequestPart("images") List<MultipartFile> images) {
        return new RspTemplate<>(HttpStatus.OK, "프로필 이미지 등록이 완료되었습니다.", userSignUpProfileService.uploadProfileImages(registrationToken, images));
    }

    // 회원가입 완료
    @Operation(summary = "회원가입 완료", description = "임시 회원가입 데이터를 실제 users 데이터로 이관하고 회원가입을 완료합니다.")
    @PostMapping("/signup/complete")
    public RspTemplate<SignUpCompleteRspDto> completeSignUp(@RequestBody SignUpTokenReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "회원가입이 완료되었습니다.", userAuthService.completeSignUp(reqDto.getRegistrationToken()));
    }

    // 아이디와 비밀번호로 로그인
    @Operation(summary = "로그인", description = "아이디와 비밀번호를 검증하고 액세스 토큰을 발급합니다.")
    @PostMapping("/login")
    public RspTemplate<LoginRspDto> login(@Valid @RequestBody LoginReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "로그인이 완료되었습니다.", userAuthService.login(reqDto));
    }
}
