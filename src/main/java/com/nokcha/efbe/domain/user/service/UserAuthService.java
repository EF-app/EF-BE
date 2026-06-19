package com.nokcha.efbe.domain.user.service;

import com.nokcha.efbe.common.auth.jwt.JwtTokenProvider;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.log.entity.LoginFailureReason;
import com.nokcha.efbe.domain.log.service.UserLoginLogService;
import com.nokcha.efbe.domain.payment.entity.UserInkFund;
import com.nokcha.efbe.domain.payment.repository.UserInkFundRepository;
import com.nokcha.efbe.domain.policy.entity.PolicyType;
import com.nokcha.efbe.domain.profile.entity.*;
import com.nokcha.efbe.domain.suspension.dto.response.UserSuspensionRspDto;
import com.nokcha.efbe.domain.suspension.service.SuspensionService;
import com.nokcha.efbe.domain.profile.repository.ProfileRepository;
import com.nokcha.efbe.domain.profile.repository.UserCustomKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserKeywordRepository;
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
import com.nokcha.efbe.domain.user.entity.RevokedToken;
import com.nokcha.efbe.domain.user.repository.ProfileImageRepository;
import com.nokcha.efbe.domain.user.repository.RevokedTokenRepository;
import com.nokcha.efbe.domain.user.repository.UserActivityStatusRepository;
import com.nokcha.efbe.domain.user.repository.CodePersonalRepository;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpCustomKeywordRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpKeywordRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAuthService {

    private static final String USER_ROLE = "ROLE_USER";

    private final AdminAccountRepository adminAccountRepository;
    private final UserRepository userRepository;
    private final UserSignUpSessionRepository userSignUpSessionRepository;
    private final AreaRepository areaRepository;
    private final UserSignUpProfileRepository userSignUpProfileRepository;
    private final UserSignUpKeywordRepository userSignUpKeywordRepository;
    private final UserSignUpCustomKeywordRepository userSignUpCustomKeywordRepository;
    private final UserSignUpPersonalRepository userSignUpPersonalRepository;
    private final CodePersonalRepository codePersonalRepository;
    private final ProfileImageRepository profileImageRepository;
    private final ProfileRepository profileRepository;
    private final UserCustomKeywordRepository userCustomKeywordRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final UserPersonalRepository userPersonalRepository;
    private final UserActivityStatusRepository userActivityStatusRepository;
    private final UserTermsRepository userTermsRepository;
    private final UserInkFundRepository userInkFundRepository;
    private final UserLoginLogService userLoginLogService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RevokedTokenRepository revokedTokenRepository;
    private final SuspensionService suspensionService;

    // 로그인 아이디 사용 가능 여부
    @Transactional(readOnly = true)
    public boolean isLoginIdAvailable(String loginId) {
        return !userRepository.existsByLoginId(loginId);
    }

    // 닉네임 사용 가능 여부
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    // 약관 동의
    @Transactional
    public SignUpProgressRspDto agreeTerms(TermsAgreementReqDto reqDto, HttpServletRequest request) {
        validateTermsRequest(reqDto);
        LocalDateTime now = LocalDateTime.now();

        UserSignUpSession signUpSession = userSignUpSessionRepository.save(UserSignUpSession.builder()
                .serviceTermsAgreed(reqDto.isServiceTermsAgreed())
                .privacyCollectionAgreed(reqDto.isPrivacyPolicyAgreed())
                .sensitiveInfoAgreed(reqDto.isSensitiveInfoAgreed())
                .noDisclosureAgreed(reqDto.isNoDisclosureAgreed())
                .locationAgreed(reqDto.isLocationAgreed())
                .ageConfirmed(false)
                .femaleConfirmed(false)
                .marketingAgreed(reqDto.isMarketingAgreed())
                .pushAgreed(reqDto.isPushAgreed())
                .serviceTermsVersion(reqDto.getServiceTermsVersion())
                .privacyCollectionVersion(reqDto.getPrivacyPolicyVersion())
                .sensitiveInfoVersion(reqDto.getSensitiveInfoVersion())
                .noDisclosureVersion(reqDto.getNoDisclosureVersion())
                .locationVersion(reqDto.isLocationAgreed() ? reqDto.getLocationVersion() : null)
                .marketingVersion(reqDto.isMarketingAgreed() ? reqDto.getMarketingVersion() : null)
                .serviceTermsAgreedAt(now)
                .privacyCollectionAgreedAt(now)
                .sensitiveInfoAgreedAt(now)
                .noDisclosureAgreedAt(now)
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

        requireTermsAgreed(signUpSession);

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

    // 이메일 입력
    @Transactional
    public SignUpProgressRspDto createEmail(EmailVerificationReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        requireTermsAgreed(signUpSession);
        requirePhoneVerified(signUpSession);
        requireCredentials(signUpSession);

        validateEmailRequest(reqDto);
        signUpSession.updateEmail(reqDto.getEmail(), LocalDateTime.now());

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

        requireTermsAgreed(signUpSession);
        requirePhoneVerified(signUpSession);

        if (userRepository.existsByLoginId(reqDto.getLoginId()) || adminAccountRepository.existsByLoginId(reqDto.getLoginId())) {
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

        requireCredentials(signUpSession);

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

        requireNickname(signUpSession);

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

        requireArea(signUpSession);

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
        LocalDate birth = LocalDate.of(1990, 01, 01);   // 임시 값 (핸드폰 인증 작성 시 수정 필요)

        validateSignUpSessionForCompletion(signUpSession);

        if (userRepository.existsByLoginId(signUpSession.getLoginId()) || adminAccountRepository.existsByLoginId(signUpSession.getLoginId())) {
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
                .lastNicknameChangedAt(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                .build());

        saveFinalProfile(user.getId(), signUpSession.getId(), signUpSession.getPurpose());
        saveUserKeywords(user.getId(), signUpSession.getId());
        saveUserCustomKeywords(user.getId(), signUpSession.getId());
        saveUserPersonals(user.getId(), signUpSession.getId());
        saveUserActivityStatus(user.getId());
        saveUserInkFund(user.getId());
        saveUserTerms(user.getId(), signUpSession);

        List<UserProfileImage> userProfileImages = profileImageRepository.findBySignUpSessionIdOrderBySortOrderAsc(signUpSession.getId());
        for (UserProfileImage userProfileImage : userProfileImages) {
            userProfileImage.assignToUser(user.getId());
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

        if (user.isWithdrawnOrWithdrawing()) {
            logFailure(user.getId(), reqDto, request, LoginFailureReason.WITHDRAWN);
            throw new BusinessException(ErrorCode.WITHDRAWN_USER);
        }

        if (!passwordEncoder.matches(reqDto.getPassword(), user.getPassword())) {
            logFailure(user.getId(), reqDto, request, LoginFailureReason.INVALID_PASSWORD);
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        // 제재 상태(TEMPORARY/PERMANENT) 도 로그인 자체는 허용 — 마이/고객센터/탈퇴/로그아웃 화이트리스트 접근
        suspensionService.evaluateAndUpdateStatus(user);

        if (user.getStatus() == UserStatus.TEMPORARY || user.getStatus() == UserStatus.PERMANENT) {
            logFailure(user.getId(), reqDto, request, LoginFailureReason.SUSPENDED);
        }

        user.updateLastActiveAt(LocalDateTime.now());
        logSuccess(user.getId(), reqDto, request);

        UserSuspensionRspDto suspension = suspensionService.findActiveBlockingSuspension(user.getId())
                .map(UserSuspensionRspDto::from)
                .orElseGet(UserSuspensionRspDto::inactive);

        return LoginRspDto.builder()
                .userId(user.getId())
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId(), USER_ROLE))
                .refreshToken(jwtTokenProvider.createRefreshToken(user.getId(), user.getLoginId(), USER_ROLE))
                .loginId(user.getLoginId())
                .suspension(suspension)
                .build();
    }

    @Transactional(readOnly = true)
    public TokenRefreshRspDto refreshAccessToken(RefreshTokenReqDto reqDto) {
        jwtTokenProvider.validateRefreshToken(reqDto.getRefreshToken());

        // 폐기된(로그아웃 된) refresh 토큰 차단
        if (revokedTokenRepository.existsByJti(jwtTokenProvider.getJti(reqDto.getRefreshToken()))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!USER_ROLE.equals(jwtTokenProvider.getRole(reqDto.getRefreshToken()))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findByLoginId(jwtTokenProvider.getLoginId(reqDto.getRefreshToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));

        if (user.isWithdrawnOrWithdrawing()) {
            throw new BusinessException(ErrorCode.WITHDRAWN_USER);
        }

        // TEMPORARY/PERMANENT 도 토큰 갱신 허용 — 차단 분기는 SuspensionGuardFilter 가 API 호출 시점에 판정.
        return TokenRefreshRspDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(user.getId(), user.getLoginId(), USER_ROLE))
                .loginId(user.getLoginId())
                .build();
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

    private void validateEmailRequest(EmailVerificationReqDto reqDto) {
        if (!reqDto.getEmail().equals(reqDto.getEmailConfirm())) {
            throw new BusinessException(ErrorCode.EMAIL_CONFIRM_MISMATCH);
        }
    }

    // 약관 요청의 버전 값을 검증
    private void validateTermsRequest(TermsAgreementReqDto reqDto) {
        validateRequiredVersion(reqDto.getServiceTermsVersion());
        validateRequiredVersion(reqDto.getPrivacyPolicyVersion());
        validateRequiredVersion(reqDto.getSensitiveInfoVersion());
        validateRequiredVersion(reqDto.getNoDisclosureVersion());

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
        requireTermsAgreed(signUpSession);
        requirePhoneVerified(signUpSession);
        requireCredentials(signUpSession);
        requireNickname(signUpSession);
        requireArea(signUpSession);
        requireLifestyle(signUpSession.getId());
        if (signUpSession.getPurpose() == null) {
            throw new BusinessException(ErrorCode.PURPOSE_REQUIRED);
        }

        if (profileImageRepository.findBySignUpSessionIdOrderBySortOrderAsc(signUpSession.getId()).isEmpty()) {
            throw new BusinessException(ErrorCode.PROFILE_REQUIRED);
        }
    }

    private void requireTermsAgreed(UserSignUpSession signUpSession) {
        if (!signUpSession.hasRequiredTermsAgreed()) {
            throw new BusinessException(ErrorCode.TERMS_AGREEMENT_REQUIRED);
        }
    }

    private void requirePhoneVerified(UserSignUpSession signUpSession) {
        if (!signUpSession.isPhoneVerified()) {
            throw new BusinessException(ErrorCode.PHONE_VERIFICATION_REQUIRED);
        }
    }

    private void requireCredentials(UserSignUpSession signUpSession) {
        if (signUpSession.getLoginId() == null || signUpSession.getPassword() == null) {
            throw new BusinessException(ErrorCode.CREDENTIALS_REQUIRED);
        }
    }

    private void requireNickname(UserSignUpSession signUpSession) {
        if (signUpSession.getNickname() == null || signUpSession.getNickname().isBlank()) {
            throw new BusinessException(ErrorCode.NICKNAME_REQUIRED);
        }
    }

    private void requireArea(UserSignUpSession signUpSession) {
        if (signUpSession.getAreaId() == null) {
            throw new BusinessException(ErrorCode.AREA_REQUIRED);
        }
    }

    private void requireLifestyle(Long signUpSessionId) {
        List<Long> selfPersonalIds = userSignUpPersonalRepository.findBySignUpSessionId(signUpSessionId).stream()
                .filter(personal -> personal.getPersonalType() == UserPersonalType.SELF)
                .map(UserSignUpPersonal::getPersonalId)
                .toList();

        if (selfPersonalIds.isEmpty()) {
            throw new BusinessException(ErrorCode.ALCOHOL_REQUIRED);
        }

        List<CodePersonal> codePersonals = codePersonalRepository.findAllById(selfPersonalIds);
        boolean hasAlcohol = codePersonals.stream().anyMatch(personal -> "음주".equals(personal.getBigCategory()));
        boolean hasSmoking = codePersonals.stream().anyMatch(personal -> "흡연".equals(personal.getBigCategory()));

        if (!hasAlcohol) {
            throw new BusinessException(ErrorCode.ALCOHOL_REQUIRED);
        }

        if (!hasSmoking) {
            throw new BusinessException(ErrorCode.SMOKING_REQUIRED);
        }
    }

    // 임시 회원가입 데이터 정리
    private void deleteTemporarySignUpData(Long signUpSessionId) {
        userSignUpProfileRepository.deleteBySignUpSessionId(signUpSessionId);
        userSignUpKeywordRepository.deleteBySignUpSessionId(signUpSessionId);
        userSignUpCustomKeywordRepository.deleteBySignUpSessionId(signUpSessionId);
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
    private int calculateKoreanAge(LocalDate birth) {
        int birthYear = birth.getYear();
        int currentYear = LocalDateTime.now().getYear();
        return currentYear - birthYear + 1;
    }

    // 유저 프로필 저장
    private void saveFinalProfile(Long userId, Long signUpSessionId, Purpose purpose) {
        UserSignUpProfile signUpProfile = userSignUpProfileRepository.findBySignUpSessionId(signUpSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_REQUIRED));

        profileRepository.findByUserId(userId)
                .ifPresentOrElse(
                        userProfile -> userProfile.update(signUpProfile.getMbti(), purpose, signUpProfile.getJob(), signUpProfile.getIdealPointTypes(), signUpProfile.getBioMessage()),
                        () -> profileRepository.save(UserProfile.builder()
                                .userId(userId)
                                .mbti(signUpProfile.getMbti())
                                .purpose(purpose)
                                .job(signUpProfile.getJob())
                                .idealPointTypes(signUpProfile.getIdealPointTypes())
                                .bioMessage(signUpProfile.getBioMessage())
                                .build())
                );
    }

    // 유저 관심사 저장
    private void saveUserKeywords(Long userId, Long signUpSessionId) {
        List<UserSignUpKeyword> signUpKeywords = userSignUpKeywordRepository.findBySignUpSessionId(signUpSessionId);

        for (UserSignUpKeyword signUpKeyword : signUpKeywords) {
            userKeywordRepository.save(UserKeyword.builder()
                    .userId(userId)
                    .keywordId(signUpKeyword.getKeywordId())
                    .build());
        }
    }

    // 유저 커스텀 관심사 저장
    private void saveUserCustomKeywords(Long userId, Long signUpSessionId) {
        List<UserSignUpCustomKeyword> signUpCustomKeywords = userSignUpCustomKeywordRepository.findBySignUpSessionId(signUpSessionId);

        for (UserSignUpCustomKeyword signUpCustomKeyword : signUpCustomKeywords) {
            userCustomKeywordRepository.save(UserCustomKeyword.builder()
                    .userId(userId)
                    .keyword(signUpCustomKeyword.getKeyword())
                    .normalizedKeyword(signUpCustomKeyword.getNormalizedKeyword())
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
        List<UserPolicy> userTerms = new ArrayList<>();
        String consentIp = signUpSession.getLastConsentIp();

        userTerms.add(buildUserTerms(userId, PolicyType.TERMS_AGREE, signUpSession.getServiceTermsVersion(), signUpSession.getServiceTermsAgreedAt(), true, consentIp));
        userTerms.add(buildUserTerms(userId, PolicyType.PRIVACY_COLLECTION_AGREE, signUpSession.getPrivacyCollectionVersion(), signUpSession.getPrivacyCollectionAgreedAt(), true, consentIp));
        userTerms.add(buildUserTerms(userId, PolicyType.SENSITIVE_AGREE, signUpSession.getSensitiveInfoVersion(), signUpSession.getSensitiveInfoAgreedAt(), true, consentIp));
        userTerms.add(buildUserTerms(userId, PolicyType.NO_DISCLOSURE_AGREE, signUpSession.getNoDisclosureVersion(), signUpSession.getNoDisclosureAgreedAt(), true, consentIp));

        if (signUpSession.isMarketingAgreed()) {
            userTerms.add(buildUserTerms(userId, PolicyType.MARKETING_AGREE, signUpSession.getMarketingVersion(), signUpSession.getMarketingAgreedAt(), false, consentIp));
        }

        if (signUpSession.isPushAgreed()) {
            userTerms.add(buildUserTerms(userId, PolicyType.PUSH_AGREE, null, signUpSession.getPushAgreedAt(), false, consentIp));
        }

        if (signUpSession.isLocationAgreed()) {
            userTerms.add(buildUserTerms(userId, PolicyType.LOCATION_AGREE, signUpSession.getLocationVersion(), signUpSession.getLocationAgreedAt(), false, consentIp));
        }

        userTermsRepository.saveAll(userTerms);
    }

    private UserPolicy buildUserTerms(Long userId, PolicyType policyType, String termsVer, LocalDateTime agreedDate, boolean isEssential, String lastConsentIp) {
        return UserPolicy.builder()
                .userId(userId)
                .policyType(policyType)
                .termsVer(termsVer)
                .agreedDate(agreedDate)
                .isEssential(isEssential)
                .lastConsentIp(lastConsentIp)
                .build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) return null;

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    // 로그아웃 — refresh + access 토큰 둘 다 jti 블랙리스트에 추가.
    // 멱등성: 토큰 검증 실패(서명/만료/형식 오류) 해도 throw 하지 않고 200 OK 반환.
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        revokeQuietly(refreshToken, "REFRESH");
        revokeQuietly(accessToken, "ACCESS");
    }

    // 토큰 1건을 blacklist 에 추가 — 어떤 예외도 무시 (멱등성 보장)
    private void revokeQuietly(String token, String tokenType) {
        if (token == null || token.isBlank()) return;
        try {
            String jti = jwtTokenProvider.getJti(token);
            if (jti == null || revokedTokenRepository.existsByJti(jti)) return;

            revokedTokenRepository.save(RevokedToken.builder()
                    .jti(jti)
                    .userId(jwtTokenProvider.getUserId(token))
                    .tokenType(tokenType)
                    .expiresAt(jwtTokenProvider.getExpiresAt(token))
                    .build());
        } catch (Exception e) {
            log.debug("[Logout] {} 토큰 폐기 실패(무시): {}", tokenType, e.getMessage());
        }
    }
}
