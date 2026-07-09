package com.nokcha.efbe.domain.profile.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.profile.dto.request.UpdateAboutMeReqDto;
import com.nokcha.efbe.domain.profile.dto.request.UpdateBasicReqDto;
import com.nokcha.efbe.domain.profile.dto.request.UpdateBioReqDto;
import com.nokcha.efbe.domain.profile.dto.request.UpdateIdealReqDto;
import com.nokcha.efbe.domain.profile.dto.request.UpdateKeywordsReqDto;
import com.nokcha.efbe.domain.profile.dto.request.UpdateLifestyleReqDto;
import com.nokcha.efbe.domain.profile.dto.request.UpdateMbtiReqDto;
import com.nokcha.efbe.domain.profile.dto.request.UpdateMyStyleReqDto;
import com.nokcha.efbe.domain.profile.dto.request.UpdatePurposeReqDto;
import com.nokcha.efbe.domain.profile.dto.response.ProfileFullRspDto;
import com.nokcha.efbe.domain.profile.entity.CodePersonal;
import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.UserCustomKeyword;
import com.nokcha.efbe.domain.profile.entity.UserKeyword;
import com.nokcha.efbe.domain.profile.entity.UserPersonal;
import com.nokcha.efbe.domain.profile.entity.UserPersonalType;
import com.nokcha.efbe.domain.profile.entity.UserProfile;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import com.nokcha.efbe.domain.profile.event.ProfileChangeKind;
import com.nokcha.efbe.domain.profile.event.ProfileUpdatedEvent;
import com.nokcha.efbe.domain.profile.repository.ProfileRepository;
import com.nokcha.efbe.domain.profile.repository.UserCustomKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserPersonalRepository;
import com.nokcha.efbe.domain.payment.model.ItemCodes;
import com.nokcha.efbe.domain.payment.service.ItemUsageService;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.CodePersonalRepository;
import com.nokcha.efbe.domain.user.repository.ProfileImageRepository;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.infra.r2.service.R2ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfileEditService {

    private static final int MAX_PROFILE_IMAGES = 5;
    private static final String PROFILE_R2_DIRECTORY = "profile";

    // 카테고리 그룹 정의 (code_personal.big_category)
    private static final Set<String> LIFESTYLE_CATEGORIES = Set.of(
            "음주", "선호 주종", "흡연", "흡연 종류", "타투유무"
    );
    // 나에 대해 — 4 개 카테고리 (일상/종교/이쪽 지인/커밍아웃)
    private static final Set<String> ABOUT_ME_CATEGORIES = Set.of(
            "일상 유형", "종교", "이쪽 지인", "커밍아웃 정도"
    );
    // 내 스타일 — 6 개 카테고리 (머리/체형/키/성향/패션/꾸미는 스타일)
    private static final Set<String> MY_STYLE_CATEGORIES = Set.of(
            "머리", "체형", "키", "성향", "패션 스타일", "꾸미는 스타일"
    );
    private static final Set<String> IDEAL_CATEGORIES = Set.of(
            "머리", "체형", "키", "성향"
    );

    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;
    private final ProfileRepository profileRepository;
    private final ProfileImageRepository profileImageRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final UserCustomKeywordRepository userCustomKeywordRepository;
    private final UserPersonalRepository userPersonalRepository;
    private final CodePersonalRepository codePersonalRepository;
    private final R2ImageService r2ImageService;
    private final ApplicationEventPublisher eventPublisher;
    private final ItemUsageService itemUsageService;

    /* ─────────── 풀 조회 ─────────── */

    @Transactional(readOnly = true)
    public ProfileFullRspDto getFullProfile() {
        Long userId = securityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        UserProfile profile = profileRepository.findByUserId(userId).orElse(null);
        CodeArea area = user.getAreaId() == null ? null
                : areaRepository.findById(user.getAreaId()).orElse(null);

        List<UserProfileImage> photos = profileImageRepository.findByUserIdOrderBySortOrderAsc(userId);
        List<UserKeyword> keywords = userKeywordRepository.findByUserId(userId);
        List<UserCustomKeyword> customKeywords = userCustomKeywordRepository.findByUserId(userId);
        List<UserPersonal> personals = userPersonalRepository.findByUserId(userId);

        return ProfileFullRspDto.builder()
                .nickname(user.getNickname())
                .areaId(user.getAreaId())
                .country(area == null ? null : area.getCountry())
                .city(area == null ? null : area.getCity())
                .age(user.getAge())
                .photos(photos.stream().map(p -> ProfileFullRspDto.PhotoItem.builder()
                        .id(p.getId())
                        .url(p.getUrl())
                        .sortOrder(p.getSortOrder())
                        .build()).toList())
                .purpose(profile == null ? null : profile.getPurpose())
                .mbti(profile == null ? null : profile.getMbti())
                .job(profile == null ? null : profile.getJob())
                .idealPointTypes(profile == null ? List.of() : profile.getIdealPointTypes())
                .bioMessage(profile == null ? null : profile.getBioMessage())
                .keywordIds(keywords.stream().map(UserKeyword::getKeywordId).toList())
                .customKeywords(customKeywords.stream().map(UserCustomKeyword::getKeyword).toList())
                .selfPersonalIds(personals.stream()
                        .filter(p -> p.getType() == UserPersonalType.SELF)
                        .map(UserPersonal::getPersonalId).toList())
                .idealPersonalIds(personals.stream()
                        .filter(p -> p.getType() == UserPersonalType.IDEAL)
                        .map(UserPersonal::getPersonalId).toList())
                .build();
    }

    /* ─────────── 섹션 1. 사진/닉네임/지역 ─────────── */

    @Transactional
    public void updateBasic(UpdateBasicReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (req.getNickname() != null && !req.getNickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(req.getNickname())) {
                throw new BusinessException(ErrorCode.ALREADY_NICKNAME);
            }
            // 월 변경 횟수 한도(무료 1 / 프리미엄 4) 소진 — item_usage_counter 원자 차감. 소진 시 NICKNAME_COOLDOWN.
            itemUsageService.consumeFreeOnly(userId, ItemCodes.NICKNAME_CHANGE, ErrorCode.NICKNAME_COOLDOWN);
            user.updateNickname(req.getNickname());
        }
        if (req.getAreaId() != null && !req.getAreaId().equals(user.getAreaId())) {
            if (!areaRepository.existsById(req.getAreaId())) {
                throw new BusinessException(ErrorCode.AREA_REQUIRED);
            }
            // 월 변경 횟수 한도(무료 1 / 프리미엄 4) 소진. 소진 시 LOCATION_COOLDOWN.
            itemUsageService.consumeFreeOnly(userId, ItemCodes.LOCATION_CHANGE, ErrorCode.LOCATION_COOLDOWN);
            user.updateAreaId(req.getAreaId());
            // 지역 변경 — 후보 풀/거리/국내·해외 그룹 자체가 바뀜 → 본인 피드 재계산
            eventPublisher.publishEvent(new ProfileUpdatedEvent(userId, ProfileChangeKind.AREA));
        }
    }

    @Transactional
    public UserProfileImage addPhoto(MultipartFile image) {
        Long userId = securityUtil.getCurrentUserId();
        long count = profileImageRepository.countByUserId(userId);
        if (count >= MAX_PROFILE_IMAGES) {
            throw new BusinessException(ErrorCode.PROFILE_IMAGE_COUNT_EXCEEDED);
        }
        int nextSortOrder = profileImageRepository.findTopByUserIdOrderBySortOrderDesc(userId)
                .map(p -> p.getSortOrder() + 1)
                .orElse(0);
        return r2ImageService.uploadProfileImageForUser(image, PROFILE_R2_DIRECTORY, userId, nextSortOrder);
    }

    @Transactional
    public void deletePhoto(Long photoId) {
        Long userId = securityUtil.getCurrentUserId();
        UserProfileImage photo = profileImageRepository.findByIdAndUserId(photoId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PHOTO));
        // R2 객체 삭제는 별도 배치 정리 (스토리지 일관성 보다 메타 일관성 우선)
        profileImageRepository.delete(photo);
    }

    /* ─────────── 섹션 2. 한 줄 소개 (bio_message) ─────────── */

    @Transactional
    public void updateBio(UpdateBioReqDto req) {
        UserProfile profile = loadOrInitProfile();
        String bio = req.getBioMessage();
        profile.updateBio(bio == null || bio.isBlank() ? null : bio.trim());
    }

    /* ─────────── 섹션 3. 관심 대상 ─────────── */

    @Transactional
    public void updatePurpose(UpdatePurposeReqDto req) {
        UserProfile profile = loadOrInitProfile();
        profile.updatePurpose(req.getPurpose());
    }

    /* ─────────── 섹션 4. 관심사 키워드 ─────────── */

    @Transactional
    public void updateKeywords(UpdateKeywordsReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        userKeywordRepository.deleteByUserId(userId);
        userCustomKeywordRepository.deleteByUserId(userId);

        List<Long> keywordIds = req.getKeywordIds() == null ? List.of() : req.getKeywordIds();
        for (Long keywordId : keywordIds) {
            userKeywordRepository.save(UserKeyword.builder()
                    .userId(userId).keywordId(keywordId).build());
        }
        List<String> customs = req.getCustomKeywords() == null ? List.of() : req.getCustomKeywords();
        for (String kw : customs) {
            if (kw == null || kw.isBlank()) continue;
            String trimmed = kw.trim();
            userCustomKeywordRepository.save(UserCustomKeyword.builder()
                    .userId(userId)
                    .keyword(trimmed)
                    .normalizedKeyword(normalize(trimmed))
                    .build());
        }
    }

    /* ─────────── 섹션 5. 생활 습관 ─────────── */

    @Transactional
    public void updateLifestyle(UpdateLifestyleReqDto req) {
        replaceSelfPersonalsForCategoryGroup(req.getPersonalIds(), LIFESTYLE_CATEGORIES);
    }

    /* ─────────── 섹션 6. MBTI ─────────── */

    @Transactional
    public void updateMbti(UpdateMbtiReqDto req) {
        UserProfile profile = loadOrInitProfile();
        profile.updateMbti(req.getMbti());
    }

    /* ─────────── 섹션 7. 나에 대해 (일상/종교/이쪽 지인/커밍아웃) ─────────── */

    @Transactional
    public void updateAboutMe(UpdateAboutMeReqDto req) {
        replaceSelfPersonalsForCategoryGroup(req.getPersonalIds(), ABOUT_ME_CATEGORIES);
    }

    /* ─────────── 섹션 8. 내 스타일 (머리/체형/키/성향/패션/꾸미는 스타일) ─────────── */

    @Transactional
    public void updateMyStyle(UpdateMyStyleReqDto req) {
        replaceSelfPersonalsForCategoryGroup(req.getPersonalIds(), MY_STYLE_CATEGORIES);
    }

    /* ─────────── 섹션 9. 내 이상형 ─────────── */

    @Transactional
    public void updateIdeal(UpdateIdealReqDto req) {
        Long userId = securityUtil.getCurrentUserId();

        // personalIds 카테고리 검증 — IDEAL 허용 카테고리 안에 있어야 함
        List<Long> personalIds = req.getPersonalIds() == null ? List.of() : req.getPersonalIds();
        validatePersonalIdsInCategories(personalIds, IDEAL_CATEGORIES);

        // type=IDEAL 전체 삭제 후 재삽입
        userPersonalRepository.deleteByUserIdAndType(userId, UserPersonalType.IDEAL);
        for (Long personalId : personalIds) {
            userPersonalRepository.save(UserPersonal.builder()
                    .userId(userId)
                    .personalId(personalId)
                    .type(UserPersonalType.IDEAL)
                    .build());
        }

        // idealPointTypes 동시 갱신
        UserProfile profile = loadOrInitProfile();
        List<IdealPointType> before =
                profile.getIdealPointTypes() == null ? List.of() : profile.getIdealPointTypes();
        List<IdealPointType> next =
                req.getIdealPointTypes() == null ? List.of() : req.getIdealPointTypes();
        profile.updateIdealPointTypes(next);

        // 이상형 중요포인트 변경은 즉시 재계산 안 함 (어뷰즈 통로 차단).
        // 다음 04:00 배치 때 sortKey 가중치 반영. before/next 비교는 향후 다른 트리거 필요 시 활용.
    }

    /* ─────────── 내부 헬퍼 ─────────── */

    // SELF 의 한 카테고리 그룹만 일괄 교체.
    private void replaceSelfPersonalsForCategoryGroup(List<Long> requestPersonalIds, Set<String> categoryGroup) {
        Long userId = securityUtil.getCurrentUserId();
        List<Long> personalIds = requestPersonalIds == null ? List.of() : requestPersonalIds;

        validatePersonalIdsInCategories(personalIds, categoryGroup);

        // 같은 카테고리에 속한 모든 personalId 목록 (기존 row 중 삭제 대상 식별용)
        List<CodePersonal> categoryPersonals = codePersonalRepository.findByBigCategoryIn(categoryGroup);
        List<Long> categoryPersonalIds = categoryPersonals.stream().map(CodePersonal::getId).toList();

        if (!categoryPersonalIds.isEmpty()) {
            userPersonalRepository.deleteByUserIdAndTypeAndPersonalIdIn(
                    userId, UserPersonalType.SELF, categoryPersonalIds);
        }
        for (Long personalId : personalIds) {
            userPersonalRepository.save(UserPersonal.builder()
                    .userId(userId)
                    .personalId(personalId)
                    .type(UserPersonalType.SELF)
                    .build());
        }
    }

    private void validatePersonalIdsInCategories(List<Long> personalIds, Set<String> allowedCategories) {
        if (personalIds.isEmpty()) return;
        List<CodePersonal> fetched = codePersonalRepository.findAllById(personalIds);
        if (fetched.size() != new HashSet<>(personalIds).size()) {
            throw new BusinessException(ErrorCode.PERSONAL_NOT_FOUND);
        }
        for (CodePersonal cp : fetched) {
            if (!allowedCategories.contains(cp.getBigCategory())) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST);
            }
        }
    }

    private UserProfile loadOrInitProfile() {
        Long userId = securityUtil.getCurrentUserId();
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROFILE));
    }

    private String normalize(String s) {
        return s == null ? null : s.toLowerCase().replaceAll("\\s+", "");
    }
}
