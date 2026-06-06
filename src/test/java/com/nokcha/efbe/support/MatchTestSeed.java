package com.nokcha.efbe.support;

import com.nokcha.efbe.domain.match.model.MatchActionType;
import com.nokcha.efbe.domain.match.entity.MatchAction;
import com.nokcha.efbe.domain.profile.entity.ProfileStatus;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import com.nokcha.efbe.domain.profile.entity.UserProfile;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 통합 테스트용 시드 빌더 — entity 직접 persist.
 *  매 테스트 메서드가 필요한 row 만 골라 만든다 (전역 시드 X).
 *  @Transactional 롤백 덕에 다음 테스트엔 흔적 안 남음.
 */
@RequiredArgsConstructor
public class MatchTestSeed {

    private final EntityManager em;

    public User activeUser(String loginIdSuffix, int age, Long areaId, LocalDateTime lastActiveAt) {
        return user(loginIdSuffix, age, areaId, UserStatus.ACTIVE, lastActiveAt);
    }

    public User user(String loginIdSuffix, int age, Long areaId, UserStatus status,
                     LocalDateTime lastActiveAt) {
        User u = User.builder()
                .uuid(UUID.randomUUID().toString())
                .loginId("itest_" + loginIdSuffix)
                .password("encoded-password-placeholder")
                .phone("0100000" + uniq(loginIdSuffix))
                .nickname("닉_" + loginIdSuffix)
                .birth(LocalDate.now().minusYears(age))
                .age(age)
                .areaId(areaId)
                .status(status)
                .lastActiveAt(lastActiveAt)
                .build();
        em.persist(u);
        return u;
    }

    public UserProfile approvedProfile(Long userId, Purpose purpose) {
        UserProfile p = UserProfile.builder()
                .userId(userId)
                .purpose(purpose)
                .build();
        em.persist(p);
        // builder 가 profileStatus 를 APPROVED 로 초기화 (UserProfile.java 확인 완료)
        return p;
    }

    public UserProfile profile(Long userId, Purpose purpose, ProfileStatus status) {
        UserProfile p = approvedProfile(userId, purpose);
        if (status != ProfileStatus.APPROVED) {
            if (status == ProfileStatus.REJECTED) p.reject("test", 0L);
        }
        return p;
    }

    public MatchAction action(Long actorId, Long targetId, MatchActionType type,
                              LocalDateTime expiresAt) {
        MatchAction a = MatchAction.builder()
                .actorId(actorId)
                .targetId(targetId)
                .actionType(type)
                .expiresAt(expiresAt)
                .build();
        em.persist(a);
        return a;
    }

    /** 대표 사진 시드 — sort_order=0. */
    public UserProfileImage profileImage(Long userId, String url) {
        return profileImage(userId, url, 0);
    }

    public UserProfileImage profileImage(Long userId, String url, int sortOrder) {
        UserProfileImage img = UserProfileImage.builder()
                .userId(userId)
                .originalName("seed-" + sortOrder + ".jpg")
                .storedName("seed-" + sortOrder)
                .sortOrder(sortOrder)
                .url(url)
                .build();
        em.persist(img);
        return img;
    }

    /** 차단(block) 양방향 검증용 — 우리 Block 엔티티가 User 참조라 EntityManager 가 다 처리. */
    public void block(User blocker, User blocked) {
        com.nokcha.efbe.domain.block.entity.Block b =
                com.nokcha.efbe.domain.block.entity.Block.builder()
                        .blocker(blocker)
                        .blocked(blocked)
                        .build();
        em.persist(b);
    }

    public void flush() {
        em.flush();
    }

    /** UNIQUE(phone/nickname/login_id) 충돌 회피용 짧은 hash. */
    private static String uniq(String s) {
        return String.format("%07d", Math.abs(s.hashCode()) % 10_000_000);
    }
}
