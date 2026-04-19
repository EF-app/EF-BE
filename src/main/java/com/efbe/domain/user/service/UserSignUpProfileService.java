package com.efbe.domain.user.service;

import com.efbe.common.auth.jwt.JwtTokenProvider;
import com.efbe.common.exception.BusinessException;
import com.efbe.common.exception.ErrorCode;
import com.efbe.domain.user.dto.request.SignUpProfileReqDto;
import com.efbe.domain.user.dto.response.SignUpProfileRspDto;
import com.efbe.domain.user.entity.Interest;
import com.efbe.domain.user.entity.Personal;
import com.efbe.domain.user.entity.ProfileImage;
import com.efbe.domain.user.entity.SignUpStep;
import com.efbe.domain.user.entity.UserSignUpCustomInterest;
import com.efbe.domain.user.entity.UserSignUpInterest;
import com.efbe.domain.user.entity.UserSignUpInterestType;
import com.efbe.domain.user.entity.UserSignUpPersonal;
import com.efbe.domain.user.entity.UserSignUpPersonalType;
import com.efbe.domain.user.entity.UserSignUpProfile;
import com.efbe.domain.user.entity.UserSignUpSession;
import com.efbe.domain.user.repository.InterestRepository;
import com.efbe.domain.user.repository.PersonalRepository;
import com.efbe.domain.user.repository.ProfileImageRepository;
import com.efbe.domain.user.repository.UserSignUpCustomInterestRepository;
import com.efbe.domain.user.repository.UserSignUpInterestRepository;
import com.efbe.domain.user.repository.UserSignUpPersonalRepository;
import com.efbe.domain.user.repository.UserSignUpProfileRepository;
import com.efbe.domain.user.repository.UserSignUpSessionRepository;
import com.efbe.infra.r2.service.R2ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSignUpProfileService {

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

    // 프로필 정보(관심자, 스타일 등) 저장
    @Transactional
    public SignUpProfileRspDto createProfile(SignUpProfileReqDto reqDto) {
        validateProfileRequest(reqDto);

        UserSignUpSession signUpSession = getAvailableSignUpSession(reqDto.getRegistrationToken());

        if (signUpSession.getSignUpStep() != SignUpStep.PURPOSE_SELECTED
                && signUpSession.getSignUpStep() != SignUpStep.PROFILE_COMPLETED) {
            throw new BusinessException(ErrorCode.PURPOSE_REQUIRED);
        }

        validateInterestIds(reqDto.getInterestIds());
        List<Personal> personals = validatePersonalIds(reqDto.getPersonalIds());
        List<Personal> idealPersonals = validatePersonalIds(reqDto.getIdealPersonalIds());
        validateRequiredPersonalCategories(personals);
        validateIdealPersonalCategories(idealPersonals);

        saveProfile(signUpSession.getId(), reqDto);
        saveInterests(signUpSession.getId(), reqDto.getInterestIds());
        saveCustomKeywords(signUpSession.getId(), reqDto.getCustomKeywords());
        savePersonals(signUpSession.getId(), reqDto.getPersonalIds(), reqDto.getIdealPersonalIds());

        return SignUpProfileRspDto.builder()
                .registrationToken(reqDto.getRegistrationToken())
                .step(signUpSession.getSignUpStep().name())
                .imageUrls(Collections.emptyList())
                .build();
    }

    // 프로필 이미지 저장
    @Transactional
    public SignUpProfileRspDto uploadProfileImages(String registrationToken, List<MultipartFile> images) {
        UserSignUpSession signUpSession = getAvailableSignUpSession(registrationToken);

        if (signUpSession.getSignUpStep() != SignUpStep.PURPOSE_SELECTED
                && signUpSession.getSignUpStep() != SignUpStep.PROFILE_COMPLETED) {
            throw new BusinessException(ErrorCode.PURPOSE_REQUIRED);
        }

        if (userSignUpProfileRepository.findBySignUpSessionId(signUpSession.getId()).isEmpty()) {
            throw new BusinessException(ErrorCode.PROFILE_REQUIRED);
        }

        validateProfileImages(images);
        List<String> imageUrls = saveProfileImages(signUpSession.getId(), images);
        signUpSession.updateProfileStep();

        return SignUpProfileRspDto.builder()
                .registrationToken(registrationToken)
                .step(signUpSession.getSignUpStep().name())
                .imageUrls(imageUrls)
                .build();
    }

    // 프로필 요청 값 검증
    private void validateProfileRequest(SignUpProfileReqDto reqDto) {
        if (reqDto.getMessage() == null || reqDto.getMessage().isBlank()) {
            throw new BusinessException(ErrorCode.INTRODUCTION_REQUIRED);
        }

        if (reqDto.getIdealPersonalIds() != null && reqDto.getIdealPersonalIds().size() > 6) {
            throw new BusinessException(ErrorCode.IDEAL_PERSONAL_COUNT_INVALID);
        }
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

    // 관심사 존재 여부 검증
    private void validateInterestIds(List<Long> interestIds) {
        if (interestIds == null || interestIds.isEmpty()) {
            return;
        }

        List<Interest> interests = interestRepository.findAllById(interestIds);

        if (interests.size() != interestIds.size()) {
            throw new BusinessException(ErrorCode.INTEREST_NOT_FOUND);
        }
    }

    // 성향 존재 여부 검증
    private List<Personal> validatePersonalIds(List<Long> personalIds) {
        if (personalIds == null || personalIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Personal> personals = personalRepository.findAllById(personalIds);

        if (personals.size() != personalIds.size()) {
            throw new BusinessException(ErrorCode.PERSONAL_NOT_FOUND);
        }

        return personals;
    }

    // 필수 성향 카테고리 존재 여부 검증
    private void validateRequiredPersonalCategories(List<Personal> personals) {
        boolean hasAlcohol = personals.stream()
                .filter(personal -> "음주".equals(personal.getBigCategory()))
                .anyMatch(personal -> {
                    if (personal.getId() < 1 || personal.getId() > 12) {
                        throw new BusinessException(ErrorCode.INVALID_ALCOHOL_ID);
                    }
                    return true;
                });

        boolean hasSmoking = personals.stream()
                .filter(personal -> "흡연".equals(personal.getBigCategory()))
                .anyMatch(personal -> {
                    if (personal.getId() < 13 || personal.getId() > 21) {
                        throw new BusinessException(ErrorCode.INVALID_SMOKING_ID);
                    }
                    return true;
                });

        boolean hasTattoo = personals.stream()
                .filter(personal -> "타투유무".equals(personal.getBigCategory()))
                .anyMatch(personal -> {
                    if (personal.getId() < 22 || personal.getId() > 26) {
                        throw new BusinessException(ErrorCode.INVALID_TATTOO_ID);
                    }
                    return true;
                });

        if (!hasAlcohol) {
            throw new BusinessException(ErrorCode.ALCOHOL_REQUIRED);
        }

        if (!hasSmoking) {
            throw new BusinessException(ErrorCode.SMOKING_REQUIRED);
        }

        if (!hasTattoo) {
            throw new BusinessException(ErrorCode.TATTOO_REQUIRED);
        }
    }

    // 이상형 카테고리 유효성 검증
    private void validateIdealPersonalCategories(List<Personal> idealPersonals) {
        for (Personal personal : idealPersonals) {
            String bigCategory = personal.getBigCategory();

            if (!"머리".equals(bigCategory)
                    && !"체형".equals(bigCategory)
                    && !"키".equals(bigCategory)
                    && !"성향".equals(bigCategory)) {
                throw new BusinessException(ErrorCode.INVALID_IDEAL_PERSONAL_CATEGORY);
            }
        }
    }

    // 회원가입 프로필 정보 저장
    private void saveProfile(Long signUpSessionId, SignUpProfileReqDto reqDto) {
        userSignUpProfileRepository.findBySignUpSessionId(signUpSessionId)
                .ifPresentOrElse(
                        profile -> profile.updateMessage(reqDto.getMessage()),
                        () -> userSignUpProfileRepository.save(UserSignUpProfile.builder()
                                .signUpSessionId(signUpSessionId)
                                .message(reqDto.getMessage())
                                .build())
                );
    }

    // 회원가입 관심사 정보 저장
    private void saveInterests(Long signUpSessionId, List<Long> interestIds) {
        userSignUpInterestRepository.deleteBySignUpSessionId(signUpSessionId);

        List<UserSignUpInterest> interests = new ArrayList<>();

        if (interestIds == null || interestIds.isEmpty()) {
            return;
        }

        for (Long interestId : interestIds) {
            interests.add(UserSignUpInterest.builder()
                    .signUpSessionId(signUpSessionId)
                    .interestId(interestId)
                    .interestType(UserSignUpInterestType.INTEREST)
                    .build());
        }

        userSignUpInterestRepository.saveAll(interests);
    }

    // 회원가입 커스텀 관심사 저장
    private void saveCustomKeywords(Long signUpSessionId, List<String> customKeywords) {
        userSignUpCustomInterestRepository.deleteBySignUpSessionId(signUpSessionId);

        if (customKeywords == null || customKeywords.isEmpty()) {
            return;
        }

        List<UserSignUpCustomInterest> interests = new ArrayList<>();

        for (String customKeyword : customKeywords) {
            if (customKeyword == null || customKeyword.isBlank()) {
                continue;
            }

            interests.add(UserSignUpCustomInterest.builder()
                    .signUpSessionId(signUpSessionId)
                    .keyword(customKeyword.trim())
                    .build());
        }

        userSignUpCustomInterestRepository.saveAll(interests);
    }

    // 회원가입 성향 정보 저장
    private void savePersonals(Long signUpSessionId, List<Long> personalIds, List<Long> idealPersonalIds) {
        userSignUpPersonalRepository.deleteBySignUpSessionId(signUpSessionId);

        List<UserSignUpPersonal> personals = new ArrayList<>();

        if (personalIds != null && !personalIds.isEmpty()) {
            for (Long personalId : personalIds) {
                personals.add(UserSignUpPersonal.builder()
                        .signUpSessionId(signUpSessionId)
                        .personalId(personalId)
                        .personalType(UserSignUpPersonalType.SELF)
                        .build());
            }
        }

        if (idealPersonalIds != null && !idealPersonalIds.isEmpty()) {
            for (Long idealPersonalId : idealPersonalIds) {
                personals.add(UserSignUpPersonal.builder()
                        .signUpSessionId(signUpSessionId)
                        .personalId(idealPersonalId)
                        .personalType(UserSignUpPersonalType.IDEAL)
                        .build());
            }
        }

        userSignUpPersonalRepository.saveAll(personals);
    }

    // 회원가입 프로필 이미지 저장
    private List<String> saveProfileImages(Long signUpSessionId, List<MultipartFile> images) {
        profileImageRepository.deleteBySignUpSessionId(signUpSessionId);

        List<ProfileImage> profileImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            ProfileImage profileImage = r2ImageService.uploadProfileImage(images.get(i), "profile", signUpSessionId, i + 1);
            profileImages.add(profileImage);
        }

        return profileImages.stream()
                .map(ProfileImage::getUrl)
                .collect(Collectors.toList());
    }
}
