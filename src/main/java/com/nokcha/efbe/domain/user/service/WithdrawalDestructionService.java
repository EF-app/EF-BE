package com.nokcha.efbe.domain.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokcha.efbe.common.util.DisplayNameUtil;
import com.nokcha.efbe.domain.chat.repository.ChatParticipantRepository;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import com.nokcha.efbe.domain.profile.repository.ProfileRepository;
import com.nokcha.efbe.domain.profile.repository.UserCustomKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserPersonalRepository;
import com.nokcha.efbe.domain.user.entity.DestructionReason;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserDestructionLog;
import com.nokcha.efbe.domain.user.entity.UserWithdrawal;
import com.nokcha.efbe.domain.user.entity.WithdrawStatus;
import com.nokcha.efbe.domain.user.repository.ProfileImageRepository;
import com.nokcha.efbe.domain.user.repository.UserDestructionLogRepository;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.domain.user.repository.UserWithdrawalRepository;
import com.nokcha.efbe.infra.r2.service.R2ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 탈퇴 완료(30일 경과) 시 개인정보 파기 — 유저 1명 단위 독립 트랜잭션.
 *
 * 파기 정책(약관·처리방침 기준):
 *  - users PII : NULL 익명화 (row 유지 — 회계 FK·통계·"탈퇴한 회원" 표시)
 *  - 하드삭제  : user_profile / user_keyword / user_custom_keyword / user_personal / user_profile_image(+R2)
 *  - 유지      : post_it / bal_vote / bal_comment / chat(Firestore) / block / report / payment_logs / user_login_log(3개월)
 *  - 외부(R2)  : best-effort — 실패해도 DB 파기는 완료하고 external_purge_status 로 재시도 배치가 보정
 *
 * 잉크 소멸 / 구독 auto_renew 는 추후작업
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalDestructionService {

    private final UserRepository userRepository;
    private final UserWithdrawalRepository userWithdrawalRepository;
    private final ProfileRepository profileRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final UserCustomKeywordRepository userCustomKeywordRepository;
    private final UserPersonalRepository userPersonalRepository;
    private final ProfileImageRepository profileImageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final R2ImageService r2ImageService;
    private final UserDestructionLogRepository userDestructionLogRepository;
    private final ObjectMapper objectMapper;

    private static final List<String> DESTROYED_FIELDS = List.of(
            "users.login_id", "users.password", "users.phone", "users.email", "users.scode",
            "users.nickname", "users.birth", "users.age", "users.fcm_token",
            "user_profile", "user_keyword", "user_custom_keyword", "user_personal", "user_profile_image(+R2)");

    private static final Map<String, String> RETAINED_ITEMS = Map.of(
            "payment_logs", "전자상거래법 5년",
            "user_login_log", "통신비밀보호법 3개월(별도 리텐션)",
            "chat", "상대방 대화 맥락 보존(발신자 익명 표시)",
            "block/report", "상대방 보호 이력");

    /**
     * 한 건의 탈퇴 요청(id)을 파기 처리. 호출자(스케줄러)가 유저별로 반복 호출한다.
     * withdrawalId 로 받아 트랜잭션 내에서 재로딩 — detached 엔티티 변경 유실 방지.
     */
    @Transactional
    public void destroy(Long withdrawalId) {
        UserWithdrawal withdrawal = userWithdrawalRepository.findById(withdrawalId).orElse(null);
        if (withdrawal == null || withdrawal.getStatus() != WithdrawStatus.REQUESTED) {
            return; // 이미 처리됨/철회됨 — 재실행 안전(멱등)
        }

        Long userId = withdrawal.getUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            // 유저가 이미 없으면 요청만 완료 처리 (파기 로그는 남길 대상 정보 없음)
            withdrawal.complete(null, null, LocalDateTime.now());
            log.warn("[WithdrawalDestruction] user 없음 — withdrawalId={}, userId={}", withdrawalId, userId);
            return;
        }
        if (user.isWithdrawn()) {
            return; // 이미 파기됨 — 재실행 안전(멱등)
        }

        LocalDateTime now = LocalDateTime.now();
        PurgeCounts counts = purgeUserData(user); // R2/하드삭제/익명화 공통 파기

        // 탈퇴 요청 완료 처리 + 이력 적재 (withdrawnAt = 신청 시각)
        withdrawal.complete(null, null, now);
        saveDestructionLog(user, DestructionReason.USER_WITHDRAW, withdrawal.getId(),
                withdrawal.getRequestedAt(), now, counts);
    }

    /**
     * 2년 미접속 휴면 계정 파기 — 탈퇴 요청 레코드 없이 시스템이 직접 파기(사유 DORMANT_2Y).
     * 스케줄러가 유저별로 반복 호출 — 독립 트랜잭션, 멱등.
     * (30일 전 통지 단계는 추후 작업 — 현재는 2년 경과 즉시 파기.)
     */
    @Transactional
    public void destroyDormant(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.isWithdrawn() || user.isWithdrawing()) {
            return; // 없거나 이미 파기/유예 중 — 재실행 안전(멱등)
        }

        LocalDateTime now = LocalDateTime.now();
        // 휴면 기준 시점(= 마지막 활동). NOT NULL 컬럼이라 만약을 대비해 now 로 폴백.
        LocalDateTime withdrawnAt = user.getLastActiveAt() != null ? user.getLastActiveAt() : now;
        PurgeCounts counts = purgeUserData(user);

        // 휴면 파기는 탈퇴 요청 레코드가 없음 → withdrawalId = null
        saveDestructionLog(user, DestructionReason.DORMANT_2Y, null, withdrawnAt, now, counts);
    }

    /** R2 사진 삭제 + 연관 테이블 하드삭제 + 채팅 닉네임 익명화 + users PII 익명화. 파기 사유와 무관하게 공통. */
    private PurgeCounts purgeUserData(User user) {
        Long userId = user.getId();

        // 1) R2 프로필 사진 삭제 (외부, best-effort) — DB 삭제 전에 URL 확보
        List<UserProfileImage> images = profileImageRepository.findByUserIdOrderBySortOrderAsc(userId);
        int photoOk = 0;
        int photoFail = 0;
        for (UserProfileImage img : images) {
            try {
                r2ImageService.deleteByUrl(img.getUrl());
                photoOk++;
            } catch (Exception e) {
                photoFail++;
                log.warn("[WithdrawalDestruction] R2 삭제 실패 — userId={}, url={}, err={}",
                        userId, img.getUrl(), e.getMessage());
            }
        }
        String purgeStatus = photoFail == 0 ? "DONE" : (photoOk > 0 ? "PARTIAL" : "FAILED");

        // 2) 연관 테이블 하드삭제 (이미지는 위에서 로드한 리스트 재사용 — 재조회 방지)
        profileImageRepository.deleteAll(images);
        profileRepository.deleteByUserId(userId);
        userKeywordRepository.deleteByUserId(userId);
        userCustomKeywordRepository.deleteByUserId(userId);
        userPersonalRepository.deleteByUserId(userId);

        // 2-1) 유지 테이블의 비정규화 닉네임 스냅샷 익명화 — 대화 맥락은 남기고 발신자만 "탈퇴한 회원"
        chatParticipantRepository.anonymizeDisplayNameByUserId(userId, DisplayNameUtil.WITHDRAWN_LABEL);

        // 3) [HOOK] 잉크 소멸(fund=0 + InkTransaction FORFEIT) / 구독 auto_renew OFF — payment 도메인 안정화 후 연결
        // 4) users PII 익명화 + WITHDRAWN
        user.anonymize();

        return new PurgeCounts(photoOk, photoFail, purgeStatus);
    }

    /** 파기 이력 적재 (영구, append-only). withdrawalId 는 사용자 신청 파기만 존재, 휴면 파기는 null. */
    private void saveDestructionLog(User user, DestructionReason reason, Long withdrawalId,
                                    LocalDateTime withdrawnAt, LocalDateTime destroyedAt, PurgeCounts counts) {
        userDestructionLogRepository.save(UserDestructionLog.builder()
                .withdrawalId(withdrawalId)
                .userId(user.getId())
                .userUuid(user.getUuid())
                .withdrawnAt(withdrawnAt)
                .destroyedAt(destroyedAt)
                .destructionReason(reason)
                .destroyedFields(toJson(DESTROYED_FIELDS))
                .retainedItems(toJson(RETAINED_ITEMS))
                .photoCountDestroyed(counts.photoOk())
                .photoCountFailed(counts.photoFail())
                .externalPurgeStatus(counts.purgeStatus())
                .operatorId(0L) // 시스템 배치
                .build());

        log.info("[WithdrawalDestruction] 파기 완료 — userId={}, reason={}, photos ok/fail={}/{}, purge={}",
                user.getId(), reason, counts.photoOk(), counts.photoFail(), counts.purgeStatus());
    }

    /** 파기 시 처리한 사진 개수/외부 파기 상태 캐리어. */
    private record PurgeCounts(int photoOk, int photoFail, String purgeStatus) {}

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("[WithdrawalDestruction] 로그 JSON 직렬화 실패 — 생략", e);
            return null;
        }
    }
}
