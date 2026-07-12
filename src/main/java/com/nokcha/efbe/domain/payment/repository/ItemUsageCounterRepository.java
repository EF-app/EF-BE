package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.ItemUsageCounter;
import com.nokcha.efbe.domain.payment.entity.ItemUsageCounterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemUsageCounterRepository extends JpaRepository<ItemUsageCounter, ItemUsageCounterId> {

    Optional<ItemUsageCounter> findByUserIdAndItemCodeAndPeriodKey(Long userId, String itemCode, String periodKey);

    /** 행이 없으면 0으로 생성(idempotent). tryConsume 전에 호출해 행 존재 보장. */
    @Modifying
    @Query(value = "INSERT INTO item_usage_counter " +
            "(user_id, item_code, period_key, used_count, create_time, update_time) " +
            "VALUES (:userId, :itemCode, :periodKey, 0, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE user_id = user_id", nativeQuery = true)
    void ensureRow(@Param("userId") Long userId, @Param("itemCode") String itemCode,
                   @Param("periodKey") String periodKey);

    /**
     * 무료 몫 원자 차감 — 한도 미만일 때만 +1. 반환 1 = 무료 사용 성공, 0 = 한도 소진(→ 잉크 폴백).
     * limit 은 저장하지 않고 사용 시점 등급으로 code_item 에서 읽어 전달.
     */
    @Modifying
    @Query("UPDATE ItemUsageCounter c SET c.usedCount = c.usedCount + 1 " +
            "WHERE c.userId = :userId AND c.itemCode = :itemCode AND c.periodKey = :periodKey " +
            "AND c.usedCount < :limit")
    int tryConsume(@Param("userId") Long userId, @Param("itemCode") String itemCode,
                   @Param("periodKey") String periodKey, @Param("limit") int limit);
}
