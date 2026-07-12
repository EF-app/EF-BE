package com.nokcha.efbe.domain.admin.user.service;

import com.nokcha.efbe.common.util.LocationUtil;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.suspension.dto.response.AdminSuspensionRspDto;
import com.nokcha.efbe.domain.admin.suspension.repository.AdminSuspensionRepository;
import com.nokcha.efbe.domain.admin.suspension.service.AdminSuspensionService;
import com.nokcha.efbe.domain.admin.user.dto.response.AdminUserDetailRspDto;
import com.nokcha.efbe.domain.admin.user.dto.response.AdminUserProfileRspDto;
import com.nokcha.efbe.domain.admin.user.dto.response.AdminUserSummaryRspDto;
import com.nokcha.efbe.domain.suspension.entity.UserSuspension;
import com.nokcha.efbe.domain.suspension.service.SuspensionService;
import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.log.entity.UserLoginLog;
import com.nokcha.efbe.domain.log.repository.UserLoginLogRepository;
import com.nokcha.efbe.domain.profile.entity.CodeKeyword;
import com.nokcha.efbe.domain.profile.entity.CodePersonal;
import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.ProfileStatus;
import com.nokcha.efbe.domain.profile.entity.UserCustomKeyword;
import com.nokcha.efbe.domain.profile.entity.UserKeyword;
import com.nokcha.efbe.domain.profile.entity.UserPersonal;
import com.nokcha.efbe.domain.profile.entity.UserPersonalType;
import com.nokcha.efbe.domain.profile.entity.UserProfile;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import com.nokcha.efbe.domain.profile.repository.ProfileRepository;
import com.nokcha.efbe.domain.profile.repository.UserCustomKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserPersonalRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import com.nokcha.efbe.domain.user.entity.UserWithdrawal;
import com.nokcha.efbe.domain.user.entity.UserDestructionLog;
import com.nokcha.efbe.domain.user.repository.UserDestructionLogRepository;
import com.nokcha.efbe.domain.user.repository.CodeKeywordRepository;
import com.nokcha.efbe.domain.user.repository.CodePersonalRepository;
import com.nokcha.efbe.domain.user.repository.ProfileImageRepository;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.domain.user.repository.UserWithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfileImageRepository profileImageRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;

    private final UserDestructionLogRepository userDestructionLogRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserInkFundRepository userInkFundRepository;
    private final PaymentLogRepository paymentLogRepository;

    private final UserLoginLogRepository userLoginLogRepository;
    private final AreaRepository areaRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final UserCustomKeywordRepository userCustomKeywordRepository;
    private final CodeKeywordRepository codeKeywordRepository;
    private final UserPersonalRepository userPersonalRepository;
    private final CodePersonalRepository codePersonalRepository;
    private final AdminSuspensionRepository adminSuspensionRepository;
    private final AdminSuspensionService adminSuspensionService;
    private final SuspensionService suspensionService;

    private static final Map<String, String> KEYWORD_GROUP = Map.of(
            "라이프스타일", "lifestyle",
            "취미", "hobby",
            "외부 여가 활동", "outdoor",
            "자기계발", "self_improve",
            "음식", "food",
            "운동", "sports",
            "음악", "music",
            "게임", "game"
    );

    // 목록 — keyword(닉네임/로그인ID/UUID LIKE) + status(UserStatus enum) 동적 필터.
    @Transactional(readOnly = true)
    public Page<AdminUserSummaryRspDto> getUsers(String keyword, UserStatus status, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        List<UserStatus> statuses = (status == null) ? List.of(UserStatus.values()) : List.of(status);
        Page<User> page = userRepository.searchForAdmin(kw, statuses, pageable);

        // 지역명 배치 조회 — areaId N개를 한 번에 (N+1 방지)
        Map<Long, CodeArea> areaMap = loadAreaMap(page.getContent().stream()
                .map(User::getAreaId)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        // 프로필 상태 배치 조회
        List<Long> userIds = page.getContent().stream().map(User::getId).toList();
        Map<Long, ProfileStatus> profileStatusMap = profileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, UserProfile::getProfileStatus));

        return page.map(u -> AdminUserSummaryRspDto.from(
                u,
                composeArea(u.getAreaId(), areaMap),
                profileStatusMap.get(u.getId())
        ));
    }

    // 단건 상세 — 기본정보 + 지역 + 프로필(키워드/성향 포함) + 결제 집계 + 접속 이력.
    @Transactional(readOnly = true)
    public AdminUserDetailRspDto getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        String area = user.getAreaId() == null ? null : LocationUtil.composeLocation(areaRepository.findById(user.getAreaId()).orElse(null));

        UserProfile profile = profileRepository.findByUserId(id).orElse(null);
        AdminUserProfileRspDto profileDto = buildProfile(id, profile);

        List<UserProfileImage> photos = profileImageRepository.findByUserIdOrderBySortOrderAsc(id);
        List<UserLoginLog> loginLogs = userLoginLogRepository.findTop20ByUserIdOrderByLoginAtDesc(id);
        // user_withdrawal 이 없는 파기 유저(휴면 파기 DORMANT_2Y 등)는 파기 이력의 destroyed_at 으로 폴백
        LocalDateTime withdrawAt = userWithdrawalRepository.findByUserId(id)
                .map(UserWithdrawal::getRequestedAt)
                .orElseGet(() -> userDestructionLogRepository.findTopByUserIdOrderByDestroyedAtDesc(id)
                        .map(UserDestructionLog::getDestroyedAt)
                        .orElse(null));

        // 결제 집계/구독/잉크 — 신규 결제 도메인(payment) 미연동 → 안전 기본값. 필요 시 배선.
        BigDecimal paymentTotal = null;
        LocalDateTime premiumUntil = null;
        boolean premium = false;
        Integer inkBalance = 0;

        // 제재 이력 — 전체 + 활성 차단 제재 1건
        List<UserSuspension> suspensions = adminSuspensionRepository
                .searchForAdmin(id, null, null, null, null, null,
                        PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id")))
                .getContent();
        List<AdminSuspensionRspDto> suspensionDtos = adminSuspensionService.toDtoList(suspensions);
        // activeSuspension 은 "차단" 의미 — TEMPORARY/PERMANENT 만 (도메인 SuspensionService 가 WARNING 제외).
        AdminSuspensionRspDto activeSuspension = suspensionService
                .findActiveBlockingSuspension(id)
                .map(adminSuspensionService::toDto)
                .orElse(null);

        // 자동 에스컬레이션 예고용 메타: 30일 내 WARNING 카운트 + 직전 TEMPORARY 일수
        long recentWarningCount = adminSuspensionRepository.countRecentWarnings(
                id, LocalDateTime.now().minusDays(AdminSuspensionService.WARNING_WINDOW_DAYS));
        Long lastTemporaryDurationDays = adminSuspensionRepository
                .findLatestTemporaryByUserId(id, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .filter(s -> s.getEndsAt() != null)
                .map(s -> ChronoUnit.DAYS.between(s.getStartsAt(), s.getEndsAt()))
                .orElse(null);

        return AdminUserDetailRspDto.of(user, area, profileDto, photos, loginLogs,
                withdrawAt, paymentTotal, premium, premiumUntil, inkBalance,
                activeSuspension, suspensionDtos,
                recentWarningCount, lastTemporaryDurationDays);
    }

    // ── 프로필 조립 — user_profile + user_keyword + user_custom_keyword + user_personal ──
    private AdminUserProfileRspDto buildProfile(Long userId, UserProfile profile) {
        if (profile == null) return null;

        // 관심사 키워드 (user_keyword → code_keyword, big_category 그룹핑)
        List<UserKeyword> userKeywords = userKeywordRepository.findByUserId(userId);
        Map<Long, CodeKeyword> kwCodes = codeKeywordRepository.findAllById(
                        userKeywords.stream().map(UserKeyword::getKeywordId).distinct().toList())
                .stream().collect(Collectors.toMap(CodeKeyword::getId, Function.identity()));
        Map<String, List<String>> keywords = new LinkedHashMap<>();
        for (UserKeyword uk : userKeywords) {
            CodeKeyword ck = kwCodes.get(uk.getKeywordId());
            if (ck == null) continue;
            String groupKey = KEYWORD_GROUP.get(ck.getBigCategory());
            if (groupKey == null) continue;
            keywords.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(ck.getSmallCategory());
        }

        // 나만의 태그 (user_custom_keyword)
        List<String> myTags = userCustomKeywordRepository.findByUserId(userId).stream()
                .map(UserCustomKeyword::getKeyword)
                .toList();

        // 성향(SELF) / 이상형(IDEAL) (user_personal → code_personal, big_category 그룹핑)
        List<UserPersonal> personals = userPersonalRepository.findByUserId(userId);
        Map<Long, CodePersonal> pCodes = codePersonalRepository.findAllById(
                        personals.stream().map(UserPersonal::getPersonalId).distinct().toList())
                .stream().collect(Collectors.toMap(CodePersonal::getId, Function.identity()));
        Map<String, List<String>> self = groupPersonal(personals, pCodes, UserPersonalType.SELF);
        Map<String, List<String>> ideal = groupPersonal(personals, pCodes, UserPersonalType.IDEAL);

        return AdminUserProfileRspDto.builder()
                .mbti(profile.getMbti() == null ? null : profile.getMbti().name())
                .matchPurpose(profile.getPurpose() == null ? null : profile.getPurpose().name())
                .interestTarget(profile.getPurpose())
                .job(profile.getJob() == null ? null : profile.getJob().name())
                .bioMessage(profile.getBioMessage())
                .idealPoints(profile.getIdealPointTypes() == null ? List.of()
                        : profile.getIdealPointTypes().stream().map(IdealPointType::name).toList())
                .keywords(keywords)
                .myTags(myTags)
                .drinking(first(self.get("음주")))
                .drinkTypes(self.getOrDefault("선호 주종", List.of()))
                .smoking(first(self.get("흡연")))
                .smokeTypes(self.getOrDefault("흡연 종류", List.of()))
                .tattoo(first(self.get("타투유무")))
                .hairStyle(first(self.get("머리")))
                .bodyType(first(self.get("체형")))
                .height(first(self.get("키")))
                .vibe(first(self.get("성향")))
                .dailyType(first(self.get("일상 유형")))
                .religion(first(self.get("종교")))
                .friendsAround(first(self.get("이쪽 지인")))
                .comingOut(first(self.get("커밍아웃 정도")))
                .fashion(first(self.get("패션 스타일")))
                .grooming(first(self.get("꾸미는 스타일")))
                .idealHair(first(ideal.get("머리")))
                .idealBody(first(ideal.get("체형")))
                .idealHeight(first(ideal.get("키")))
                .idealVibe(first(ideal.get("성향")))
                .profileStatus(profile.getProfileStatus() == null
                        ? null : profile.getProfileStatus().name())
                .profileRejectedReason(profile.getProfileRejectedReason())
                .profileReviewedAt(profile.getProfileReviewedAt())
                .profileReviewedBy(profile.getProfileReviewedBy())
                .build();
    }

    // 프로필 승인
    @Transactional
    public AdminUserDetailRspDto approveProfile(Long userId, Long reviewerAdminId) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROFILE));
        profile.approve(reviewerAdminId);
        return getUser(userId);
    }

    // 프로필 반려 — 사유는 유저에게 노출됨
    @Transactional
    public AdminUserDetailRspDto rejectProfile(Long userId, String reason, Long reviewerAdminId) {
        UserProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROFILE));
        profile.reject(reason, reviewerAdminId);
        return getUser(userId);
    }

    // user_personal 을 type 별로 big_category → small_category 목록으로 그룹핑.
    private Map<String, List<String>> groupPersonal(List<UserPersonal> personals,
                                                    Map<Long, CodePersonal> codeMap,
                                                    UserPersonalType type) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (UserPersonal up : personals) {
            if (up.getType() != type) continue;
            CodePersonal cp = codeMap.get(up.getPersonalId());
            if (cp == null) continue;
            result.computeIfAbsent(cp.getBigCategory(), k -> new ArrayList<>()).add(cp.getSmallCategory());
        }
        return result;
    }

    private static String first(List<String> list) {
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    private Map<Long, CodeArea> loadAreaMap(List<Long> areaIds) {
        if (areaIds.isEmpty()) return Map.of();
        return areaRepository.findAllById(areaIds).stream()
                .collect(Collectors.toMap(CodeArea::getId, Function.identity()));
    }

    private String composeArea(Long areaId, Map<Long, CodeArea> areaMap) {
        if (areaId == null) return null;
        return LocationUtil.composeLocation(areaMap.get(areaId));
    }
}
