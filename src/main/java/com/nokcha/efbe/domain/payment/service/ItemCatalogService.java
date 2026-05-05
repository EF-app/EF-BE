package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.dto.response.ItemCatalogRspDto;
import com.nokcha.efbe.domain.payment.entity.CodeItem;
import com.nokcha.efbe.domain.payment.entity.ItemCategory;
import com.nokcha.efbe.domain.payment.repository.CodeItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 아이템 마스터 조회 서비스 (변경 빈도 낮음 - 캐시 적용)
@Service
@RequiredArgsConstructor
public class ItemCatalogService {

    private final CodeItemRepository itemCatalogRepository;

    // 활성 아이템 카테고리별 조회 (null 이면 전체)
    @Cacheable(value = "itemList", key = "#category == null ? 'ALL' : #category.name()")
    @Transactional(readOnly = true)
    public List<ItemCatalogRspDto> getItems(ItemCategory category) {
        List<CodeItem> items = (category == null)
                ? itemCatalogRepository.findAll()
                : itemCatalogRepository.findByCategoryAndIsActiveTrueOrderByStarCostAsc(category);
        return items.stream().map(ItemCatalogRspDto::from).toList();
    }

    // 아이템 코드 단건 조회
    @Cacheable(value = "itemByCode", key = "#code")
    @Transactional(readOnly = true)
    public ItemCatalogRspDto getByCode(String code) {
        CodeItem item = itemCatalogRepository.findByItemCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ITEM));
        return ItemCatalogRspDto.from(item);
    }
}
