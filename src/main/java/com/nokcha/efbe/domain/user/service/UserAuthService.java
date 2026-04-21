package com.nokcha.efbe.domain.user.service;

import com.nokcha.efbe.common.auth.jwt.JwtTokenProvider;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.efbe.domain.profile.entity.*;
import com.nokcha.efbe.domain.profile.entity.*;
import com.nokcha.efbe.domain.profile.repository.ProfileRepository;
import com.nokcha.efbe.domain.profile.repository.UserInterestRepository;
import com.nokcha.efbe.domain.profile.repository.UserPersonalRepository;
import com.nokcha.efbe.domain.user.dto.request.LoginReqDto;
import com.nokcha.efbe.domain.user.dto.request.PhoneVerificationReqDto;
import com.nokcha.efbe.domain.user.dto.request.SignUpBasicInfoReqDto;
import com.nokcha.efbe.domain.user.dto.request.SignUpCredentialsReqDto;
import com.nokcha.efbe.domain.user.dto.request.SignUpPurposeReqDto;
import com.nokcha.efbe.domain.user.dto.request.TermsAgreementReqDto;
import com.nokcha.efbe.domain.user.dto.response.LoginRspDto;
import com.nokcha.efbe.domain.user.dto.response.SignUpCompleteRspDto;
import com.nokcha.efbe.domain.user.dto.response.SignUpProgressRspDto;
import com.efbe.domain.user.entity.*;
import com.nokcha.efbe.domain.user.entity.*;
import com.nokcha.efbe.domain.user.repository.ProfileImageRepository;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpCustomInterestRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpInterestRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpPersonalRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpProfileRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final UserSignUpSessionRepository userSignUpSessionRepository;
    private final UserSignUpProfileRepository userSignUpProfileRepository;
    private final UserSignUpInterestRepository userSignUpInterestRepository;
    private final UserSignUpCustomInterestRepository userSignUpCustomInterestRepository;
    private final UserSignUpPersonalRepository userSignUpPersonalRepository;
    private final ProfileImageRepository profileImageRepository;
    private final ProfileRepository profileRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserPersonalRepository userPersonalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 약관 동의
    @Transactional
    public SignUpProgressRspDto agreeTerms(TermsAgreementReqDto reqDto) {
        UserSignUpSession signUpSession = userSignUpSessionRepository.save(UserSignUpSession.builder()
                .serviceTermsAgreed(reqDto.isServiceTermsAgreed())
                .privacyPolicyAgreed(reqDto.isPrivacyPolicyAgreed())
                .ageConfirmed(false)
                .femaleConfirmed(false)
                .marketingAgreed(reqDto.isMarketingAgreed())
                .signUpStep(SignUpStep.TERMS_AGREED)
                .expiredAt(LocalDateTime.now().plusDays(1))
                .completed(false)
                .build());

        String registrationToken = jwtTokenProvider.createRegistrationToken(signUpSession.getId());

        return SignUpProgressRspDto.builder()
                .registrationToken(registrationToken)
                .step(signUpSession.getSignUpStep().name())
                .expiredAt(signUpSession.getExpiredAt())
                .build();
    }

    // 휴대폰 인증
    @Transactional
    public SignUpProgressRspDto verifyPhone(PhoneVerificationReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (!signUpSession.hasRequiredTermsAgreed()) {
            throw new BusinessException(ErrorCode.TERMS_AGREEMENT_REQUIRED);
        }

        validatePhoneVerificationRequest(reqDto);

        if (userRepository.existsByPhone(reqDto.getPhone())) {
            throw new BusinessException(ErrorCode.ALREADY_PHONE);
        }

        signUpSession.verifyPhone(
                reqDto.getPhone(),
                reqDto.isAdultVerified(),
                reqDto.isFemaleVerified(),
                LocalDateTime.now()
        );

        return SignUpProgressRspDto.builder()
                .registrationToken(reqDto.getRegistrationToken())
                .step(signUpSession.getSignUpStep().name())
                .expiredAt(signUpSession.getExpiredAt())
                .build();
    }

    // 아이디 비밀번호 생성
    @Transactional
    public SignUpProgressRspDto createCredentials(SignUpCredentialsReqDto reqDto) {
        validateCredentialsRequest(reqDto);

        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (!signUpSession.hasRequiredTermsAgreed()) {
            throw new BusinessException(ErrorCode.TERMS_AGREEMENT_REQUIRED);
        }

        if (!signUpSession.isPhoneVerified()) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_REQUIRED);
        }

        if (userRepository.existsByLoginId(reqDto.getLoginId())) {
            throw new BusinessException(ErrorCode.ALREADY_USER);
        }

        signUpSession.updateCredentials(reqDto.getLoginId(), passwordEncoder.encode(reqDto.getPassword()));

        return SignUpProgressRspDto.builder()
                .registrationToken(reqDto.getRegistrationToken())
                .step(signUpSession.getSignUpStep().name())
                .expiredAt(signUpSession.getExpiredAt())
                .build();
    }

    // 닉네임과 지역 정보 저장
    @Transactional
    public SignUpProgressRspDto createBasicInfo(SignUpBasicInfoReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (signUpSession.getSignUpStep() != SignUpStep.CREDENTIALS_COMPLETED
                && signUpSession.getSignUpStep() != SignUpStep.BASIC_INFO_COMPLETED) {
            throw new BusinessException(ErrorCode.CREDENTIALS_REQUIRED);
        }

        String nickname = reqDto.getNickname().trim();

        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.ALREADY_NICKNAME);
        }

        signUpSession.updateBasicInfo(nickname, reqDto.getAreaId());

        return SignUpProgressRspDto.builder()
                .registrationToken(reqDto.getRegistrationToken())
                .step(signUpSession.getSignUpStep().name())
                .expiredAt(signUpSession.getExpiredAt())
                .build();
    }

    // 가입 목적 저장
    @Transactional
    public SignUpProgressRspDto createPurpose(SignUpPurposeReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (signUpSession.getSignUpStep() != SignUpStep.BASIC_INFO_COMPLETED
                && signUpSession.getSignUpStep() != SignUpStep.PURPOSE_SELECTED) {
            throw new BusinessException(ErrorCode.BASIC_INFO_REQUIRED);
        }

        Purpose purpose = reqDto.getPurpose();
        if (purpose == null) {
            throw new BusinessException(ErrorCode.PURPOSE_REQUIRED);
        }

        signUpSession.updatePurpose(purpose);

        return SignUpProgressRspDto.builder()
                .registrationToken(reqDto.getRegistrationToken())
                .step(signUpSession.getSignUpStep().name())
                .expiredAt(signUpSession.getExpiredAt())
                .build();
    }

    // 회원가입 완료 처리
    @Transactional
    public SignUpCompleteRspDto completeSignUp(String registrationToken) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(registrationToken);

        if (signUpSession.getSignUpStep() != SignUpStep.PROFILE_COMPLETED) {
            throw new BusinessException(ErrorCode.PROFILE_REQUIRED);
        }


        validateSignUpSessionForCompletion(signUpSession);

        if (userRepository.existsByLoginId(signUpSession.getLoginId())) {
            throw new BusinessException(ErrorCode.ALREADY_USER);
        }

        if (userRepository.existsByPhone(signUpSession.getPhone())) {
            throw new BusinessException(ErrorCode.ALREADY_PHONE);
        }

        if (userRepository.existsByNickname(signUpSession.getNickname())) {
            throw new BusinessException(ErrorCode.ALREADY_NICKNAME);
        }

        User user = userRepository.save(User.builder()
                .uuid(generateUuid())
                .loginId(signUpSession.getLoginId())
                .password(signUpSession.getPassword())
                .birth(19900101)
                .scode("0000")
                .phone(signUpSession.getPhone())
                .nickname(signUpSession.getNickname())
                .areaId(signUpSession.getAreaId())
                .purpose(signUpSession.getPurpose())
                .isWithdraw(false)
                .lastNicknameChangeTime(LocalDateTime.now())
                .role(Role.ROLE_USER)
                .banStatus(BanStatus.NONE)
                .build());

        saveFinalProfile(user.getId(), signUpSession.getId(), signUpSession.getPurpose());
        saveUserInterests(user.getId(), signUpSession.getId());
        saveUserPersonals(user.getId(), signUpSession.getId());

        List<ProfileImage> profileImages = profileImageRepository
                .findBySignUpSessionIdOrderBySortOrderAsc(signUpSession.getId());

        for (ProfileImage profileImage : profileImages) {
            profileImage.assignToUser(user.getId());
        }

        deleteTemporarySignUpData(signUpSession.getId());
        signUpSession.completeSignUp();

        return SignUpCompleteRspDto.builder()
                .userId(user.getId())
                .step(signUpSession.getSignUpStep().name())
                .completed(true)
                .build();
    }

    // 로그인
    @Transactional
    public LoginRspDto login(LoginReqDto reqDto) {
        User user = userRepository.findByLoginId(reqDto.getLoginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (user.isWithdraw()) {
            throw new BusinessException(ErrorCode.WITHDRAWN_USER);
        }

        if (!passwordEncoder.matches(reqDto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        user.updateLastLoginTime(LocalDateTime.now());

        return LoginRspDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId(), user.getRole()))
                .tokenType("Bearer")
                .loginId(user.getLoginId())
                .build();
    }

    // 휴대폰 인증 요청 값 검증
    private void validatePhoneVerificationRequest(PhoneVerificationReqDto reqDto) {
        if (!reqDto.isAdultVerified()) {
            throw new BusinessException(ErrorCode.ADULT_VERIFICATION_REQUIRED);
        }

        if (!reqDto.isFemaleVerified()) {
            throw new BusinessException(ErrorCode.FEMALE_VERIFICATION_REQUIRED);
        }
    }

    // 비밀번호 재확인 검증
    private void validateCredentialsRequest(SignUpCredentialsReqDto reqDto) {
        if (!reqDto.getPassword().equals(reqDto.getPasswordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
    }

    // 회원가입 완료 가능 여부 검증
    private void validateSignUpSessionForCompletion(UserSignUpSession signUpSession) {
        if (signUpSession.getLoginId() == null || signUpSession.getPassword() == null) {
            throw new BusinessException(ErrorCode.CREDENTIALS_REQUIRED);
        }

        if (!signUpSession.isPhoneVerified()) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_REQUIRED);
        }

        if (signUpSession.getNickname() == null || signUpSession.getNickname().isBlank()) {
            throw new BusinessException(ErrorCode.NICKNAME_REQUIRED);
        }

        if (signUpSession.getAreaId() == null) {
            throw new BusinessException(ErrorCode.AREA_REQUIRED);
        }

        if (signUpSession.getPurpose() == null) {
            throw new BusinessException(ErrorCode.PURPOSE_REQUIRED);
        }
    }

    // 임시 회원가입 데이터 정리
    private void deleteTemporarySignUpData(Long signUpSessionId) {
        userSignUpProfileRepository.deleteBySignUpSessionId(signUpSessionId);
        userSignUpInterestRepository.deleteBySignUpSessionId(signUpSessionId);
        userSignUpCustomInterestRepository.deleteBySignUpSessionId(signUpSessionId);
        userSignUpPersonalRepository.deleteBySignUpSessionId(signUpSessionId);
    }

    // 사용 가능한 회원가입 세션 조회
    private UserSignUpSession getAvailableSignUpSession(String registrationToken) {
        jwtTokenProvider.validateToken(registrationToken);

        if (!jwtTokenProvider.isRegistrationToken(registrationToken)) {
            throw new BusinessException(ErrorCode.INVALID_REGISTRATION_TOKEN);
        }

        Long signUpSessionId = jwtTokenProvider.getSignupSessionId(registrationToken);

        UserSignUpSession signUpSession = userSignUpSessionRepository.findByIdAndCompletedFalse(signUpSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SIGNUP_SESSION_NOT_FOUND));

        if (signUpSession.isExpired(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EXPIRED_REGISTRATION_TOKEN);
        }

        return signUpSession;
    }

    // UUID 생성
    private String generateUuid() {
        return java.util.UUID.randomUUID().toString();
    }

    // 유저 프로필 저장
    private void saveFinalProfile(Long userId, Long signUpSessionId, Purpose purpose) {
        UserSignUpProfile signUpProfile = userSignUpProfileRepository.findBySignUpSessionId(signUpSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_REQUIRED));

        profileRepository.findByUserId(userId)
                .ifPresentOrElse(
                        profile -> profile.update(signUpProfile.getMbti(), purpose, signUpProfile.getMessage()),
                        () -> profileRepository.save(Profile.builder()
                                .userId(userId)
                                .mbti(signUpProfile.getMbti())
                                .purpose(purpose)
                                .message(signUpProfile.getMessage())
                                .build())
                );
    }

    // 유저 관심사 저장
    private void saveUserInterests(Long userId, Long signUpSessionId) {
        List<UserSignUpInterest> signUpInterests = userSignUpInterestRepository.findBySignUpSessionId(signUpSessionId);

        for (UserSignUpInterest signUpInterest : signUpInterests) {
            userInterestRepository.save(UserInterest.builder()
                    .userId(userId)
                    .interestId(signUpInterest.getInterestId())
                    .build());
        }
    }

    // 유저 및 이상형 스타일 저장
    private void saveUserPersonals(Long userId, Long signUpSessionId) {
        List<UserSignUpPersonal> signUpPersonals = userSignUpPersonalRepository.findBySignUpSessionId(signUpSessionId);

        for (UserSignUpPersonal signUpPersonal : signUpPersonals) {
            userPersonalRepository.save(UserPersonal.builder()
                    .userId(userId)
                    .personalId(signUpPersonal.getPersonalId())
                    .type(signUpPersonal.getPersonalType())
                    .build());
        }
    }
}
