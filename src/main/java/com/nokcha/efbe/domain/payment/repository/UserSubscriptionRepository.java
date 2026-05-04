package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 유저 구독 이력 레포지토리 (이력형 B — 유저당 다수 row, 활성 1행만)
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    // 유저 활성 구독 1건 (이력형이라 user_id 만으로는 다수, is_active=true 조건 필수)
    Optional<UserSubscription> findByUserIdAndIsActiveTrue(Long userId);

    // 유저 구독 이력 (마이페이지 — idx_sub_user_time)
    List<UserSubscription> findByUserIdOrderByCreateTimeDesc(Long userId);

    // 만료 임박 구독 (idx_sub_active_end 인덱스 활용)
    @Query("select s from UserSubscription s where s.isActive = true and s.endDate <= :now")
    List<UserSubscription> findDueExpire(@Param("now") LocalDateTime now);

    // 월간 보너스 지급 대상 (idx_sub_bonus_due 인덱스 활용 -
    //   활성 + 아직 유효 + (첫 지급 미수령 OR 지난 지급일이 threshold 이전))
    @Query("select s from UserSubscription s " +
            "where s.isActive = true and s.endDate > :now " +
            "and (s.lastBonusGrantAt is null or s.lastBonusGrantAt < :threshold)")
    List<UserSubscription> findDueBonusGrant(@Param("now") LocalDateTime now,
                                             @Param("threshold") LocalDateTime threshold);
}
