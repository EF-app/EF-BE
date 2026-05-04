package com.nokcha.efbe.domain.user.service;

import com.nokcha.efbe.common.auth.jwt.JwtTokenProvider;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.repository.AdminRepository;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.log.entity.LoginFailureReason;
import com.nokcha.efbe.domain.log.service.UserLoginLogService;
import com.nokcha.efbe.domain.premium.entity.UserInkFund;
import com.nokcha.efbe.domain.premium.repository.UserInkFundRepository;
import com.nokcha.efbe.domain.profile.entity.*;
import com.nokcha.efbe.domain.profile.repository.ProfileRepository;
import com.nokcha.efbe.domain.profile.repository.UserCustomInterestRepository;
import com.nokcha.efbe.domain.profile.repository.UserInterestRepository;
import com.nokcha.efbe.domain.profile.repository.UserPersonalRepository;
import com.nokcha.efbe.domain.user.dto.request.EmailVerificationReqDto;
import com.nokcha.efbe.domain.user.dto.request.LoginReqDto;
import com.nokcha.efbe.domain.user.dto.request.PhoneVerificationReqDto;
import com.nokcha.efbe.domain.user.dto.request.RefreshTokenReqDto;
import com.nokcha.efbe.domain.user.dto.request.SignUpAreaReqDto;
import com.nokcha.efbe.domain.user.dto.request.SignUpCredentialsReqDto;
import com.nokcha.efbe.domain.user.dto.request.SignUpNicknameReqDto;
import com.nokcha.efbe.domain.user.dto.request.SignUpPurposeReqDto;
import com.nokcha.efbe.domain.user.dto.request.TermsAgreementReqDto;
import com.nokcha.efbe.domain.user.dto.response.LoginRspDto;
import com.nokcha.efbe.domain.user.dto.response.SignUpCompleteRspDto;
import com.nokcha.efbe.domain.user.dto.response.SignUpProgressRspDto;
import com.nokcha.efbe.domain.user.dto.response.TokenRefreshRspDto;
import com.nokcha.efbe.domain.user.entity.*;
import com.nokcha.efbe.domain.user.repository.ProfileImageRepository;
import com.nokcha.efbe.domain.user.repository.UserActivityStatusRepository;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpCustomInterestRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpInterestRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpPersonalRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpProfileRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpSessionRepository;
import com.nokcha.efbe.domain.user.repository.UserTermsRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAuthService {

    private static final String USER_ROLE = "ROLE_USER";

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final UserSignUpSessionRepository userSignUpSessionRepository;
    private final AreaRepository areaRepository;
    private final UserSignUpProfileRepository userSignUpProfileRepository;
    private final UserSignUpInterestRepository userSignUpInterestRepository;
    private final UserSignUpCustomInterestRepository userSignUpCustomInterestRepository;
    private final UserSignUpPersonalRepository userSignUpPersonalRepository;
    private final ProfileImageRepository profileImageRepository;
    private final ProfileRepository profileRepository;
    private final UserCustomInterestRepository userCustomInterestRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserPersonalRepository userPersonalRepository;
    private final UserActivityStatusRepository userActivityStatusRepository;
    private final UserTermsRepository userTermsRepository;
    private final UserInkFundRepository userInkFundRepository;
    private final UserLoginLogService userLoginLogService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 약관 동의
    @Transactional
    public SignUpProgressRspDto agreeTerms(TermsAgreementReqDto reqDto, HttpServletRequest request) {
        validateTermsRequest(reqDto);
        LocalDateTime now = LocalDateTime.now();

        UserSignUpSession signUpSession = userSignUpSessionRepository.save(UserSignUpSession.builder()
                .serviceTermsAgreed(reqDto.isServiceTermsAgreed())
                .privacyPolicyAgreed(reqDto.isPrivacyPolicyAgreed())
                .sensitiveInfoAgreed(reqDto.isSensitiveInfoAgreed())
                .personalInformationAgreed(reqDto.isPersonalInformationAgreed())
                .locationAgreed(reqDto.isLocationAgreed())
                .ageConfirmed(false)
                .femaleConfirmed(false)
                .marketingAgreed(reqDto.isMarketingAgreed())
                .pushAgreed(reqDto.isPushAgreed())
                .serviceTermsVersion(reqDto.getServiceTermsVersion())
                .privacyPolicyVersion(reqDto.getPrivacyPolicyVersion())
                .sensitiveInfoVersion(reqDto.getSensitiveInfoVersion())
                .personalInformationVersion(reqDto.getPersonalInformationVersion())
                .locationVersion(reqDto.isLocationAgreed() ? reqDto.getLocationVersion() : null)
                .marketingVersion(reqDto.isMarketingAgreed() ? reqDto.getMarketingVersion() : null)
                .serviceTermsAgreedAt(now)
                .privacyPolicyAgreedAt(now)
                .sensitiveInfoAgreedAt(now)
                .personalInformationAgreedAt(now)
                .locationAgreedAt(reqDto.isLocationAgreed() ? now : null)
                .marketingAgreedAt(reqDto.isMarketingAgreed() ? now : null)
                .pushAgreedAt(reqDto.isPushAgreed() ? now : null)
                .lastConsentIp(resolveClientIp(request))
                .signUpStep(SignUpStep.TERMS_AGREED)
                .expiredAt(now.plusDays(1))
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

    // 이메일 인증
    @Transactional
    public SignUpProgressRspDto verifyEmail(EmailVerificationReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (!signUpSession.hasRequiredTermsAgreed()) {
            throw new BusinessException(ErrorCode.TERMS_AGREEMENT_REQUIRED);
        }

        if (!signUpSession.isPhoneVerified()) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_REQUIRED);
        }

        signUpSession.verifyEmail(reqDto.getEmail(), LocalDateTime.now());

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

        if (userRepository.existsByLoginId(reqDto.getLoginId()) || adminRepository.existsByLoginId(reqDto.getLoginId())) {
            throw new BusinessException(ErrorCode.ALREADY_USER);
        }

        signUpSession.updateCredentials(reqDto.getLoginId(), passwordEncoder.encode(reqDto.getPassword()));

        return SignUpProgressRspDto.builder()
                .registrationToken(reqDto.getRegistrationToken())
                .step(signUpSession.getSignUpStep().name())
                .expiredAt(signUpSession.getExpiredAt())
                .build();
    }

    // 닉네임 저장
    @Transactional
    public SignUpProgressRspDto createNickname(SignUpNicknameReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (signUpSession.getSignUpStep() != SignUpStep.CREDENTIALS_COMPLETED
                && signUpSession.getSignUpStep() != SignUpStep.NICKNAME_COMPLETED) {
            throw new BusinessException(ErrorCode.CREDENTIALS_REQUIRED);
        }

        String nickname = reqDto.getNickname().trim();

        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.ALREADY_NICKNAME);
        }

        signUpSession.updateNickname(nickname);

        return SignUpProgressRspDto.builder()
                .registrationToken(reqDto.getRegistrationToken())
                .step(signUpSession.getSignUpStep().name())
                .expiredAt(signUpSession.getExpiredAt())
                .build();
    }

    // 지역 저장
    @Transactional
    public SignUpProgressRspDto createArea(SignUpAreaReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (signUpSession.getSignUpStep() != SignUpStep.NICKNAME_COMPLETED
                && signUpSession.getSignUpStep() != SignUpStep.AREA_COMPLETED) {
            throw new BusinessException(ErrorCode.NICKNAME_REQUIRED);
        }

        if (!areaRepository.existsById(reqDto.getAreaId())) {
            throw new BusinessException(ErrorCode.AREA_REQUIRED);
        }

        signUpSession.updateArea(reqDto.getAreaId());

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

        if (!isPurposeEditableStep(signUpSession.getSignUpStep())) {
            throw new BusinessException(ErrorCode.AREA_REQUIRED);
        }

        if (reqDto.getPurpose() == null) {
            throw new BusinessException(ErrorCode.PURPOSE_REQUIRED);
        }

        signUpSession.updatePurpose(reqDto.getPurpose());

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
        int birth = 19900101;   // 임시 값 (핸드폰 인증 작성 시 수정 필요)

        if (signUpSession.getSignUpStep() != SignUpStep.PROFILE_COMPLETED) {
            throw new BusinessException(ErrorCode.PROFILE_REQUIRED);
        }

        validateSignUpSessionForCompletion(signUpSession);

        if (userRepository.existsByLoginId(signUpSession.getLoginId()) || adminRepository.existsByLoginId(signUpSession.getLoginId())) {
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
                .birth(birth)
                .age(calculateKoreanAge(birth))
                .scode(null)
                .phone(signUpSession.getPhone())
                .email(signUpSession.getEmail())
                .nickname(signUpSession.getNickname())
                .areaId(signUpSession.getAreaId())
                .isWithdraw(false)
                .lastNicknameChangeTime(LocalDateTime.now())
                .banStatus(BanStatus.NONE)
                .build());

        saveFinalProfile(user.getId(), signUpSession.getId(), signUpSession.getPurpose());
        saveUserInterests(user.getId(), signUpSession.getId());
        saveUserCustomInterests(user.getId(), signUpSession.getId());
        saveUserPersonals(user.getId(), signUpSession.getId());
        saveUserActivityStatus(user.getId());
        saveUserInkFund(user.getId());
        saveUserTerms(user.getId(), signUpSession);

        List<ProfileImage> profileImages = profileImageRepository.findBySignUpSessionIdOrderBySortOrderAsc(signUpSession.getId());
        for (ProfileImage profileImage : profileImages) {
            profileImage.assignToUser(user.getId());
        }

        deleteTemporarySignUpData(signUpSession.getId());
        signUpSession.completeSignUp();

        String completedStep = signUpSession.getSignUpStep().name();
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId(), USER_ROLE);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getLoginId(), USER_ROLE);
        userSignUpSessionRepository.delete(signUpSession);

        return SignUpCompleteRspDto.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .loginId(user.getLoginId())
                .step(completedStep)
                .completed(true)
                .build();
    }

    // 로그인
    @Transactional
    public LoginRspDto login(LoginReqDto reqDto, HttpServletRequest request) {
        User user = userRepository.findByLoginId(reqDto.getLoginId()).orElse(null);

        if (user == null) {
            logFailure(null, reqDto, request, LoginFailureReason.INVALID_ID);
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        if (user.isWithdraw()) {
            logFailure(user.getId(), reqDto, request, LoginFailureReason.WITHDRAWN);
            throw new BusinessException(ErrorCode.WITHDRAWN_USER);
        }

        validateBanStatus(user, reqDto, request);

        if (!passwordEncoder.matches(reqDto.getPassword(), user.getPassword())) {
            logFailure(user.getId(), reqDto, request, LoginFailureReason.INVALID_PASSWORD);
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        user.updateLastLoginTime(LocalDateTime.now());
        logSuccess(user.getId(), reqDto, request);

        return LoginRspDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId(), USER_ROLE))
                .refreshToken(jwtTokenProvider.createRefreshToken(user.getId(), user.getLoginId(), USER_ROLE))
                .loginId(user.getLoginId())
                .build();
    }

    @Transactional(readOnly = true)
    public TokenRefreshRspDto refreshAccessToken(RefreshTokenReqDto reqDto) {
        jwtTokenProvider.validateRefreshToken(reqDto.getRefreshToken());

        if (!USER_ROLE.equals(jwtTokenProvider.getRole(reqDto.getRefreshToken()))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findByLoginId(jwtTokenProvider.getLoginId(reqDto.getRefreshToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));

        if (user.isWithdraw()) {
            throw new BusinessException(ErrorCode.WITHDRAWN_USER);
        }

        if (user.getBanStatus() == BanStatus.SEVEN_DAYS) {
            throw new BusinessException(ErrorCode.BANNED_USER_SEVEN_DAYS);
        }

        if (user.getBanStatus() == BanStatus.THIRTY_DAYS) {
            throw new BusinessException(ErrorCode.BANNED_USER_THIRTY_DAYS);
        }

        if (user.getBanStatus() == BanStatus.FOREVER) {
            throw new BusinessException(ErrorCode.BANNED_USER_FOREVER);
        }

        return TokenRefreshRspDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId(), USER_ROLE))
                .loginId(user.getLoginId())
                .build();
    }

    // 목적 단계 수정 가능 여부 확인
    private boolean isPurposeEditableStep(SignUpStep signUpStep) {
        return signUpStep == SignUpStep.AREA_COMPLETED || signUpStep.isAtLeast(SignUpStep.PURPOSE_SELECTED);
    }

    // 정지 상태 회원의 로그인 가능 여부 확인
    private void validateBanStatus(User user, LoginReqDto reqDto, HttpServletRequest request) {
        if (user.getBanStatus() == null || user.getBanStatus() == BanStatus.NONE) {
            return;
        }

        switch (user.getBanStatus()) {
            case SEVEN_DAYS -> {
                logFailure(user.getId(), reqDto, request, LoginFailureReason.SUSPENDED);
                throw new BusinessException(ErrorCode.BANNED_USER_SEVEN_DAYS);
            }
            case THIRTY_DAYS -> {
                logFailure(user.getId(), reqDto, request, LoginFailureReason.SUSPENDED);
                throw new BusinessException(ErrorCode.BANNED_USER_THIRTY_DAYS);
            }
            case FOREVER -> {
                logFailure(user.getId(), reqDto, request, LoginFailureReason.SUSPENDED);
                throw new BusinessException(ErrorCode.BANNED_USER_FOREVER);
            }
            default -> throw new BusinessException(ErrorCode.INVALID_USER);
        }
    }

    // 로그인 성공 이력 저장
    private void logSuccess(Long userId, LoginReqDto reqDto, HttpServletRequest request) {
        try {
            userLoginLogService.logSuccess(userId, reqDto.getLoginId(), request, reqDto.getDeviceId(), reqDto.getPlatform(), reqDto.isScodeStep());
        } catch (Exception e) {
            log.warn("로그인 성공 로그 저장 실패: loginId={}", reqDto.getLoginId(), e);
        }
    }

    // 로그인 실패 이력 저장
    private void logFailure(Long userId, LoginReqDto reqDto, HttpServletRequest request, LoginFailureReason failureReason) {
        try {
            userLoginLogService.logFailure(userId, reqDto.getLoginId(), request, reqDto.getDeviceId(), reqDto.getPlatform(), failureReason, reqDto.isScodeStep());
        } catch (Exception e) {
            log.warn("로그인 실패 로그 저장 실패: loginId={}, reason={}", reqDto.getLoginId(), failureReason, e);
        }
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

    // 약관 요청의 버전 값을 검증
    private void validateTermsRequest(TermsAgreementReqDto reqDto) {
        validateRequiredVersion(reqDto.getServiceTermsVersion());
        validateRequiredVersion(reqDto.getPrivacyPolicyVersion());
        validateRequiredVersion(reqDto.getSensitiveInfoVersion());
        validateRequiredVersion(reqDto.getPersonalInformationVersion());

        if (reqDto.isMarketingAgreed()) {
            validateRequiredVersion(reqDto.getMarketingVersion());
        }

        if (reqDto.isLocationAgreed()) {
            validateRequiredVersion(reqDto.getLocationVersion());
        }
    }

    private void validateRequiredVersion(String version) {
        if (version == null || version.isBlank()) {
            throw new BusinessException(ErrorCode.TERMS_AGREEMENT_REQUIRED);
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

    // YYYYMMDD 형식 생년월일 기준 한국 나이 계산
    private int calculateKoreanAge(int birth) {
        int birthYear = birth / 10000;
        int currentYear = LocalDateTime.now().getYear();
        return currentYear - birthYear + 1;
    }

    // 유저 프로필 저장
    private void saveFinalProfile(Long userId, Long signUpSessionId, Purpose purpose) {
        UserSignUpProfile signUpProfile = userSignUpProfileRepository.findBySignUpSessionId(signUpSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_REQUIRED));

        profileRepository.findByUserId(userId)
                .ifPresentOrElse(
                        profile -> profile.update(signUpProfile.getMbti(), purpose, signUpProfile.getJob(), signUpProfile.getIdealPointTypes(), signUpProfile.getMessage()),
                        () -> profileRepository.save(Profile.builder()
                                .userId(userId)
                                .mbti(signUpProfile.getMbti())
                                .purpose(purpose)
                                .job(signUpProfile.getJob())
                                .idealPointTypes(signUpProfile.getIdealPointTypes())
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

    // 유저 커스텀 관심사 저장
    private void saveUserCustomInterests(Long userId, Long signUpSessionId) {
        List<UserSignUpCustomInterest> signUpCustomInterests = userSignUpCustomInterestRepository.findBySignUpSessionId(signUpSessionId);

        for (UserSignUpCustomInterest signUpCustomInterest : signUpCustomInterests) {
            userCustomInterestRepository.save(UserCustomInterest.builder()
                    .userId(userId)
                    .keyword(signUpCustomInterest.getKeyword())
                    .normalizedKeyword(signUpCustomInterest.getNormalizedKeyword())
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

    // 유저 활동 상태 초기값 저장
    private void saveUserActivityStatus(Long userId) {
        userActivityStatusRepository.save(UserActivityStatus.builder()
                .userId(userId)
                .balgameVotedCount(0L)
                .balgameCommentCount(0L)
                .postitWrittenCount(0L)
                .postitReplySentCount(0L)
                .postitReplyReceivedCount(0L)
                .matchLikeReceivedCount(0L)
                .matchSuccessCount(0L)
                .build());
    }

    // 유저 잉크 잔액 정보 초기화
    private void saveUserInkFund(Long userId) {
        userInkFundRepository.save(UserInkFund.builder()
                .userId(userId)
                .fund(0)
                .totalCharged(0)
                .totalUsed(0)
                .build());
    }

    // 유저 약관 동의 정보 저장
    private void saveUserTerms(Long userId, UserSignUpSession signUpSession) {
        List<UserTerms> userTerms = new ArrayList<>();
        String consentIp = signUpSession.getLastConsentIp();

        userTerms.add(buildUserTerms(userId, TermType.TERMS_AGREE, signUpSession.getServiceTermsVersion(), signUpSession.getServiceTermsAgreedAt(), true, consentIp));
        userTerms.add(buildUserTerms(userId, TermType.PRIVACY_AGREE, signUpSession.getPrivacyPolicyVersion(), signUpSession.getPrivacyPolicyAgreedAt(), true, consentIp));
        userTerms.add(buildUserTerms(userId, TermType.SENSITIVE_AGREE, signUpSession.getSensitiveInfoVersion(), signUpSession.getSensitiveInfoAgreedAt(), true, consentIp));
        userTerms.add(buildUserTerms(userId, TermType.PERSONAL_INFORMATION_AGREE, signUpSession.getPersonalInformationVersion(), signUpSession.getPersonalInformationAgreedAt(), true, consentIp));

        if (signUpSession.isMarketingAgreed()) {
            userTerms.add(buildUserTerms(userId, TermType.MARKETING_AGREE, signUpSession.getMarketingVersion(), signUpSession.getMarketingAgreedAt(), false, consentIp));
        }

        if (signUpSession.isPushAgreed()) {
            userTerms.add(buildUserTerms(userId, TermType.PUSH_AGREE, null, signUpSession.getPushAgreedAt(), false, consentIp));
        }

        if (signUpSession.isLocationAgreed()) {
            userTerms.add(buildUserTerms(userId, TermType.LOCATION_AGREE, signUpSession.getLocationVersion(), signUpSession.getLocationAgreedAt(), false, consentIp));
        }

        userTermsRepository.saveAll(userTerms);
    }

    private UserTerms buildUserTerms(Long userId, TermType termType, String termsVer, LocalDateTime agreedDate, boolean isEssential, String lastConsentIp) {
        return UserTerms.builder()
                .userId(userId)
                .termType(termType)
                .termsVer(termsVer)
                .agreedDate(agreedDate)
                .isEssential(isEssential)
                .lastConsentIp(lastConsentIp)
                .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
