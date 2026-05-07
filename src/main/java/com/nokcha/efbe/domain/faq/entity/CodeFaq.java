package com.nokcha.efbe.domain.faq.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 도움말/FAQ 마스터 — code_faq 테이블 (코드 마스터, 운영 SQL 로 데이터 주입)
@Getter
@Entity
@Table(
        name = "code_faq",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_faq_code", columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_faq_category_active", columnList = "category, is_active, display_order"),
                @Index(name = "idx_faq_popular", columnList = "is_popular, is_active, display_order")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // FE 의 id 값("acc-1","match-1" 등) 보존 컬럼
    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private FaqCategory category;

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_popular", nullable = false)
    private Boolean isPopular;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}
