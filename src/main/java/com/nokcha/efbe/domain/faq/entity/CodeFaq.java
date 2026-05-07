package com.nokcha.efbe.domain.faq.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 도움말/FAQ 마스터 — code_faq 테이블 (코드 마스터, 운영 SQL 로 데이터 주입)
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
public class CodeFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false,
            columnDefinition = "ENUM('ACCOUNT','MATCHING','MESSAGE','PAYMENT','REPORT','ETC') NOT NULL")
    private FaqCategory category;

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT NOT NULL")
    private String answer;

    @Column(name = "display_order", nullable = false,
            columnDefinition = "INT NOT NULL DEFAULT 0")
    private Integer displayOrder;

    @Column(name = "is_popular", nullable = false,
            columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
    private Boolean isPopular;

    @Column(name = "is_active", nullable = false,
            columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false,
            columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false,
            columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;
}
