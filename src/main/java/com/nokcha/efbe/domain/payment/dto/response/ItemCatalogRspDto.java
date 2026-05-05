package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.CodeItem;
import com.nokcha.efbe.domain.payment.entity.ItemCategory;
import lombok.Builder;
import lombok.Getter;

// 아이템 마스터 응답 DTO
@Getter
@Builder
public class ItemCatalogRspDto {
    private Integer id;
    private String itemCode;
    private String name;
    private String description;
    private Integer starCost;
    private ItemCategory category;
    private Integer effectDurationMin;
    private boolean active;

    public static ItemCatalogRspDto from(CodeItem i) {
        return ItemCatalogRspDto.builder()
                .id(i.getId())
                .itemCode(i.getItemCode())
                .name(i.getName())
                .description(i.getDescription())
                .starCost(i.getStarCost())
                .category(i.getCategory())
                .effectDurationMin(i.getEffectDurationMin())
                .active(Boolean.TRUE.equals(i.getIsActive()))
                .build();
    }
}
