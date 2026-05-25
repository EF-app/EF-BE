package com.nokcha.efbe.domain.faq.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "code_faq",
        indexes = {
                @Index(name = "idx_faq_category_active", columnList = "category, is_active, display_order"),
                @Index(name = "idx_faq_popular",         columnList = "is_popular, is_active, display_order")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeFaq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private FaqCategory category;

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT NOT NULL")
    private String answer;

    @Column(name = "display_order", nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    private Integer displayOrder;

    @Column(name = "is_popular", nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
    private Boolean isPopular;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
    private Boolean isActive;
}
