package com.nokcha.efbe.domain.policy.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.user.entity.TermType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "code_policy_document")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodePolicyDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TermType policyType;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 500)
    private String summary;

    @Column(nullable = false)
    private boolean isRequired;

    @Column(nullable = false)
    private LocalDateTime effectiveDate;

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private boolean requiresReagreement;

    @Builder
    public CodePolicyDocument(TermType policyType, String version, String title, String content, String summary,
                              boolean isRequired, LocalDateTime effectiveDate, LocalDateTime expiresAt,
                              boolean isActive, boolean requiresReagreement) {
        this.policyType = policyType;
        this.version = version;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.isRequired = isRequired;
        this.effectiveDate = effectiveDate;
        this.expiresAt = expiresAt;
        this.isActive = isActive;
        this.requiresReagreement = requiresReagreement;
    }
}
