package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 아이템 마스터 엔티티 (code_item)
@Getter
@Entity
@Table(name = "code_item",
        uniqueConstraints = {@UniqueConstraint(name = "uk_item_code", columnNames = "item_code")},
        indexes = {@Index(name = "idx_item_category_active", columnList = "category, is_active")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeItem extends BaseEntity {

    // 서비스 레이어 훅에서 사용하는 표준 아이템 코드 (설계서 아이템 표 참고)
    public static final String CODE_SUPER_LIKE = "SUPER_LIKE";
    public static final String CODE_PRE_MESSAGE = "PRE_MESSAGE";
    public static final String CODE_PROFILE_BOOST = "PROFILE_BOOST";
    public static final String CODE_POST_PIN = "POST_PIN";
    public static final String CODE_UNDO = "UNDO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "item_code", nullable = false, length = 40)
    private String itemCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "star_cost", nullable = false)
    private Integer starCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 10)
    private ItemCategory category;

    @Column(name = "effect_duration_min")
    private Integer effectDurationMin;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Builder
    private CodeItem(String itemCode, String name, String description, Integer starCost,
                        ItemCategory category, Integer effectDurationMin, Boolean isActive) {
        this.itemCode = itemCode;
        this.name = name;
        this.description = description;
        this.starCost = starCost;
        this.category = category;
        this.effectDurationMin = effectDurationMin;
        this.isActive = isActive == null ? Boolean.TRUE : isActive;
    }
}
