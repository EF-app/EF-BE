package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.CodeItem;
import com.nokcha.efbe.domain.payment.entity.ItemCategory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 아이템 마스터 레포지토리 (변경 빈도 낮음 - 코드 조회는 캐시)
public interface CodeItemRepository extends JpaRepository<CodeItem, Integer> {

    // 아이템 코드 조회 - 매칭/포스트잇/구매 훅에서 반복 호출되므로 캐시
    @Cacheable(value = "itemEntityByCode", key = "#itemCode")
    Optional<CodeItem> findByItemCode(String itemCode);

    List<CodeItem> findByCategoryAndIsActiveTrueOrderByStarCostAsc(ItemCategory category);
}
