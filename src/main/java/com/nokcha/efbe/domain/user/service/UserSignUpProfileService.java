package com.nokcha.efbe.domain.user.service;

import com.nokcha.efbe.common.auth.jwt.JwtTokenProvider;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.util.InterestKeywordNormalizer;
import com.nokcha.efbe.domain.profile.entity.CodeInterest;
import com.nokcha.efbe.domain.profile.entity.CodePersonal;
import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.Mbti;
import com.nokcha.efbe.domain.profile.entity.ProfileImage;
import com.nokcha.efbe.domain.profile.entity.UserPersonalType;
import com.nokcha.efbe.domain.user.dto.request.SignUpAboutMeReqDto;
import com.nokcha.efbe.domain.user.dto.request.SignUpIdealReqDto;
import com.nokcha.efbe.domain.user.dto.request.SignUpInterestReqDto;
import com.nokcha.efbe.domain.user.dto.request.SignUpLifestyleReqDto;
import com.nokcha.efbe.domain.user.dto.response.SignUpProfileRspDto;
import com.nokcha.efbe.domain.user.entity.Job;
import com.nokcha.efbe.domain.user.entity.SignUpStep;
import com.nokcha.efbe.domain.user.entity.UserSignUpCustomInterest;
import com.nokcha.efbe.domain.user.entity.UserSignUpInterest;
import com.nokcha.efbe.domain.user.entity.UserSignUpInterestType;
import com.nokcha.efbe.domain.user.entity.UserSignUpPersonal;
import com.nokcha.efbe.domain.user.entity.UserSignUpProfile;
import com.nokcha.efbe.domain.user.entity.UserSignUpSession;
import com.nokcha.efbe.domain.user.repository.InterestRepository;
import com.nokcha.efbe.domain.user.repository.PersonalRepository;
import com.nokcha.efbe.domain.user.repository.ProfileImageRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpCustomInterestRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpInterestRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpPersonalRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpProfileRepository;
import com.nokcha.efbe.domain.user.repository.UserSignUpSessionRepository;
import com.nokcha.efbe.infra.r2.service.R2ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSignUpProfileService {

    private static final Set<String> LIFESTYLE_CATEGORIES = Set.of("음주", "선호 주종", "흡연", "흡연 종류", "타투유무");
    private static final Set<String> ABOUT_ME_CATEGORIES = Set.of("종교", "이쪽 지인", "커밍아웃 정도", "머리", "체형", "키", "성향", "패션 스타일", "꾸미는 스타일");
    private static final Set<String> IDEAL_CATEGORIES = Set.of("머리", "체형", "키", "성향");

    private final JwtTokenProvider jwtTokenProvider;
    private final UserSignUpSessionRepository userSignUpSessionRepository;
    private final UserSignUpProfileRepository userSignUpProfileRepository;
    private final UserSignUpInterestRepository userSignUpInterestRepository;
    private final UserSignUpCustomInterestRepository userSignUpCustomInterestRepository;
    private final UserSignUpPersonalRepository userSignUpPersonalRepository;
    private final ProfileImageRepository profileImageRepository;
    private final InterestRepository interestRepository;
    private final PersonalRepository personalRepository;
    private final R2ImageService r2ImageService;

    // 관심사 정보 저장
    @Transactional
    public SignUpProfileRspDto createInterests(SignUpInterestReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (!isInterestEditableStep(signUpSession.getSignUpStep())) {
            throw new BusinessException(ErrorCode.PURPOSE_REQUIRED);
        }

        validateInterestIds(reqDto.getInterestIds());
        saveInterests(signUpSession.getId(), reqDto.getInterestIds());
        saveCustomKeywords(signUpSession.getId(), reqDto.getCustomKeywords());
        signUpSession.updateInterestStep();

        return buildResponse(reqDto.getRegistrationToken(), signUpSession.getSignUpStep(), Collections.emptyList());
    }

    // 생활 습관 정보 저장
    @Transactional
    public SignUpProfileRspDto createLifestyle(SignUpLifestyleReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (!isLifestyleEditableStep(signUpSession.getSignUpStep())) {
            throw new BusinessException(ErrorCode.PROFILE_REQUIRED);
        }

        List<CodePersonal> lifestylePersonals = validatePersonalIds(reqDto.getPersonalIds());
        validatePersonalCategories(lifestylePersonals, LIFESTYLE_CATEGORIES);
        validateRequiredPersonalCategories(lifestylePersonals);
        saveSelfPersonalsByCategories(signUpSession.getId(), lifestylePersonals, LIFESTYLE_CATEGORIES);
        signUpSession.updateLifestyleStep();

        return buildResponse(reqDto.getRegistrationToken(), signUpSession.getSignUpStep(), Collections.emptyList());
    }

    // 나에 대해서 정보 저장
    @Transactional
    public SignUpProfileRspDto createAboutMe(SignUpAboutMeReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (!isAboutMeEditableStep(signUpSession.getSignUpStep())) {
            throw new BusinessException(ErrorCode.PROFILE_REQUIRED);
        }

        List<CodePersonal> aboutMePersonals = validatePersonalIds(reqDto.getPersonalIds());
        validatePersonalCategories(aboutMePersonals, ABOUT_ME_CATEGORIES);
        saveDraftProfileAboutMe(signUpSession.getId(), reqDto.getJob(), reqDto.getMbti());
        saveSelfPersonalsByCategories(signUpSession.getId(), aboutMePersonals, ABOUT_ME_CATEGORIES);
        signUpSession.updateAboutMeStep();

        return buildResponse(reqDto.getRegistrationToken(), signUpSession.getSignUpStep(), Collections.emptyList());
    }

    // 이상형 정보 저장
    @Transactional
    public SignUpProfileRspDto createIdeal(SignUpIdealReqDto reqDto) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (!isIdealEditableStep(signUpSession.getSignUpStep())) {
            throw new BusinessException(ErrorCode.PROFILE_REQUIRED);
        }

        List<CodePersonal> idealCodePersonals = validatePersonalIds(reqDto.getIdealPersonalIds());
        validateIdealPersonalCategories(idealCodePersonals);
        saveIdealPersonals(signUpSession.getId(), reqDto.getIdealPersonalIds());
        saveDraftIdealPointTypes(signUpSession.getId(), reqDto.getIdealPointTypes());
        signUpSession.updateIdealStep();

        return buildResponse(reqDto.getRegistrationToken(), signUpSession.getSignUpStep(), Collections.emptyList());
    }

    // 프로필 사진과 소개 저장
    @Transactional
    public SignUpProfileRspDto createProfile(String registrationToken, String message, List<MultipartFile> images) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(registrationToken);

        if (!isProfileIntroEditableStep(signUpSession.getSignUpStep())) {
            throw new BusinessException(ErrorCode.PROFILE_REQUIRED);
        }

        validateProfileImages(images);
        saveDraftProfileMessage(signUpSession.getId(), message);
        List<String> imageUrls = saveProfileImages(signUpSession.getId(), images);
        signUpSession.updateProfileIntroStep();

        return buildResponse(registrationToken, signUpSession.getSignUpStep(), imageUrls);
    }

    // 프로필 이미지 요청 값 검증
    private void validateProfileImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty() || images.size() > 3) {
            throw new BusinessException(ErrorCode.PROFILE_IMAGE_COUNT_EXCEEDED);
        }
    }

    // 회원가입 세션 조회
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

    // 관심사 단계 수정 가능 여부 확인
    private boolean isInterestEditableStep(SignUpStep signUpStep) {
        return signUpStep.isAtLeast(SignUpStep.PURPOSE_SELECTED);
    }

    // 생활 습관 단계 수정 가능 여부 확인
    private boolean isLifestyleEditableStep(SignUpStep signUpStep) {
        return signUpStep.isAtLeast(SignUpStep.PURPOSE_SELECTED);
    }

    // 나에 대해서 단계 수정 가능 여부 확인
    private boolean isAboutMeEditableStep(SignUpStep signUpStep) {
        return signUpStep.isAtLeast(SignUpStep.LIFESTYLE_COMPLETED);
    }

    // 이상형 단계 수정 가능 여부 확인
    private boolean isIdealEditableStep(SignUpStep signUpStep) {
        return signUpStep.isAtLeast(SignUpStep.LIFESTYLE_COMPLETED);
    }

    // 프로필 사진 단계 수정 가능 여부 확인
    private boolean isProfileIntroEditableStep(SignUpStep signUpStep) {
        return signUpStep.isAtLeast(SignUpStep.LIFESTYLE_COMPLETED);
    }

    // 관심사 존재 여부 검증
    private void validateInterestIds(List<Long> interestIds) {
        if (interestIds == null || interestIds.isEmpty()) {
            return;
        }

        List<CodeInterest> codeInterests = interestRepository.findAllById(interestIds);

        if (codeInterests.size() != interestIds.size()) {
            throw new BusinessException(ErrorCode.INTEREST_NOT_FOUND);
        }
    }

    // 성향 존재 여부 검증
    private List<CodePersonal> validatePersonalIds(List<Long> personalIds) {
        if (personalIds == null || personalIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<CodePersonal> codePersonals = personalRepository.findAllById(personalIds);

        if (codePersonals.size() != personalIds.size()) {
            throw new BusinessException(ErrorCode.PERSONAL_NOT_FOUND);
        }

        return codePersonals;
    }

    // 허용된 성향 카테고리인지 검증
    private void validatePersonalCategories(List<CodePersonal> codePersonals, Set<String> allowedCategories) {
        for (CodePersonal codePersonal : codePersonals) {
            if (!allowedCategories.contains(codePersonal.getBigCategory())) {
                throw new BusinessException(ErrorCode.PERSONAL_NOT_FOUND);
            }
        }
    }

    // 필수 생활습관 카테고리 존재 여부 검증
    private void validateRequiredPersonalCategories(List<CodePersonal> codePersonals) {
        boolean hasAlcohol = hasCategory(codePersonals, "음주");
        boolean hasSmoking = hasCategory(codePersonals, "흡연");

        if (!hasAlcohol) {
            throw new BusinessException(ErrorCode.ALCOHOL_REQUIRED);
        }

        if (!hasSmoking) {
            throw new BusinessException(ErrorCode.SMOKING_REQUIRED);
        }
    }

    // 이상형 카테고리 유효성 검증
    private void validateIdealPersonalCategories(List<CodePersonal> idealCodePersonals) {
        for (CodePersonal codePersonal : idealCodePersonals) {
            if (!IDEAL_CATEGORIES.contains(codePersonal.getBigCategory())) {
                throw new BusinessException(ErrorCode.INVALID_IDEAL_PERSONAL_CATEGORY);
            }
        }
    }

    // 특정 대분류 포함 여부 확인
    private boolean hasCategory(List<CodePersonal> codePersonals, String bigCategory) {
        return codePersonals.stream().anyMatch(codePersonal -> bigCategory.equals(codePersonal.getBigCategory()));
    }

    // 프로필 드래프트 조회하거나 생성
    private UserSignUpProfile getOrCreateDraftProfile(Long signUpSessionId) {
        return userSignUpProfileRepository.findBySignUpSessionId(signUpSessionId)
                .orElseGet(() -> userSignUpProfileRepository.save(UserSignUpProfile.builder()
                        .signUpSessionId(signUpSessionId)
                        .build()));
    }

    // 나에 대해서 프로필 정보 저장
    private void saveDraftProfileAboutMe(Long signUpSessionId, Job job, Mbti mbti) {
        UserSignUpProfile profile = getOrCreateDraftProfile(signUpSessionId);
        profile.updateAboutMe(job, mbti);
    }

    // 이상형 포인트 정보 저장
    private void saveDraftIdealPointTypes(Long signUpSessionId, List<IdealPointType> idealPointTypes) {
        UserSignUpProfile profile = getOrCreateDraftProfile(signUpSessionId);
        profile.updateIdealPointTypes(normalizeIdealPointTypes(idealPointTypes));
    }

    // 소개 문구 저장
    private void saveDraftProfileMessage(Long signUpSessionId, String message) {
        UserSignUpProfile profile = getOrCreateDraftProfile(signUpSessionId);
        profile.updateMessage(message == null || message.isBlank() ? null : message.trim());
    }

    // 이상형 포인트 목록 정규화
    private List<IdealPointType> normalizeIdealPointTypes(List<IdealPointType> idealPointTypes) {
        if (idealPointTypes == null || idealPointTypes.isEmpty()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(new LinkedHashSet<>(idealPointTypes));
    }

    // 관심사 정보 저장
    private void saveInterests(Long signUpSessionId, List<Long> interestIds) {
        userSignUpInterestRepository.deleteBySignUpSessionId(signUpSessionId);

        if (interestIds == null || interestIds.isEmpty()) return;

        List<UserSignUpInterest> interests = new ArrayList<>();
        for (Long interestId : interestIds) {
            interests.add(UserSignUpInterest.builder()
                    .signUpSessionId(signUpSessionId)
                    .interestId(interestId)
                    .interestType(UserSignUpInterestType.INTEREST)
                    .build());
        }

        userSignUpInterestRepository.saveAll(interests);
    }

    // 커스텀 관심사 저장
    private void saveCustomKeywords(Long signUpSessionId, List<String> customKeywords) {
        userSignUpCustomInterestRepository.deleteBySignUpSessionId(signUpSessionId);
        userSignUpCustomInterestRepository.flush();

        if (customKeywords == null || customKeywords.isEmpty()) return;

        Map<String, String> normalizedKeywordMap = new LinkedHashMap<>();

        for (String customKeyword : customKeywords) {
            if (customKeyword == null || customKeyword.isBlank()) {
                continue;
            }

            String keyword = customKeyword.trim();
            String normalizedKeyword = InterestKeywordNormalizer.normalize(keyword);

            if (normalizedKeyword == null) {
                continue;
            }

            normalizedKeywordMap.putIfAbsent(normalizedKeyword, keyword);
        }

        List<UserSignUpCustomInterest> interests = new ArrayList<>();
        for (Map.Entry<String, String> entry : normalizedKeywordMap.entrySet()) {
            interests.add(UserSignUpCustomInterest.builder()
                    .signUpSessionId(signUpSessionId)
                    .keyword(entry.getValue())
                    .normalizedKeyword(entry.getKey())
                    .build());
        }

        userSignUpCustomInterestRepository.saveAll(interests);
    }

    // 자기 성향 정보 카테고리별 저장
    private void saveSelfPersonalsByCategories(Long signUpSessionId, List<CodePersonal> codePersonals, Set<String> categories) {
        List<UserSignUpPersonal> allPersonals = userSignUpPersonalRepository.findBySignUpSessionId(signUpSessionId);
        Set<Long> existingSelfIds = allPersonals.stream()
                .filter(personal -> personal.getPersonalType() == UserPersonalType.SELF)
                .map(UserSignUpPersonal::getPersonalId)
                .collect(Collectors.toSet());

        List<CodePersonal> existingCodePersonals = existingSelfIds.isEmpty() ? Collections.emptyList() : personalRepository.findAllById(existingSelfIds);

        Set<Long> removableIds = existingCodePersonals.stream()
                .filter(codePersonal -> categories.contains(codePersonal.getBigCategory()))
                .map(CodePersonal::getId)
                .collect(Collectors.toSet());

        List<UserSignUpPersonal> personals = allPersonals.stream()
                .filter(personal -> !(personal.getPersonalType() == UserPersonalType.SELF && removableIds.contains(personal.getPersonalId())))
                .map(this::copySignUpPersonal)
                .collect(Collectors.toCollection(ArrayList::new));

        for (CodePersonal codePersonal : codePersonals) {
            personals.add(UserSignUpPersonal.builder()
                    .signUpSessionId(signUpSessionId)
                    .personalId(codePersonal.getId())
                    .personalType(UserPersonalType.SELF)
                    .build());
        }

        userSignUpPersonalRepository.deleteBySignUpSessionId(signUpSessionId);
        userSignUpPersonalRepository.saveAll(personals);
    }

    // 이상형 정보 저장
    private void saveIdealPersonals(Long signUpSessionId, List<Long> idealPersonalIds) {
        List<UserSignUpPersonal> allPersonals = userSignUpPersonalRepository.findBySignUpSessionId(signUpSessionId);
        List<UserSignUpPersonal> personals = allPersonals.stream()
                .filter(personal -> personal.getPersonalType() != UserPersonalType.IDEAL)
                .map(this::copySignUpPersonal)
                .collect(Collectors.toCollection(ArrayList::new));

        if (idealPersonalIds != null) {
            for (Long idealPersonalId : idealPersonalIds) {
                personals.add(UserSignUpPersonal.builder()
                        .signUpSessionId(signUpSessionId)
                        .personalId(idealPersonalId)
                        .personalType(UserPersonalType.IDEAL)
                        .build());
            }
        }

        userSignUpPersonalRepository.deleteBySignUpSessionId(signUpSessionId);
        userSignUpPersonalRepository.saveAll(personals);
    }

    // 성향 엔티티를 새 인스턴스로 복사
    private UserSignUpPersonal copySignUpPersonal(UserSignUpPersonal personal) {
        return UserSignUpPersonal.builder()
                .signUpSessionId(personal.getSignUpSessionId())
                .personalId(personal.getPersonalId())
                .personalType(personal.getPersonalType())
                .build();
    }

    // 프로필 이미지 저장
    private List<String> saveProfileImages(Long signUpSessionId, List<MultipartFile> images) {
        profileImageRepository.deleteBySignUpSessionId(signUpSessionId);

        List<ProfileImage> profileImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            ProfileImage profileImage = r2ImageService.uploadProfileImage(images.get(i), "profile", signUpSessionId, i + 1);
            profileImages.add(profileImage);
        }

        return profileImages.stream().map(ProfileImage::getUrl).collect(Collectors.toList());
    }

    // 단계 응답을 생성
    private SignUpProfileRspDto buildResponse(String registrationToken, SignUpStep signUpStep, List<String> imageUrls) {
        return SignUpProfileRspDto.builder()
                .registrationToken(registrationToken)
                .step(signUpStep.name())
                .imageUrls(imageUrls)
                .build();
    }
}
