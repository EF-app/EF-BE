package com.nokcha.efbe.domain.user.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "user_terms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_terms_user_id_term_type", columnNames = {"user_id", "term_type"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTerms extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TermType termType;

    @Column(length = 20)
    private String termsVer;

    @Column
    private LocalDateTime agreedDate;

    @Column
    private boolean isEssential;

    @Column(length = 45)
    private String lastConsentIp;

    @Builder
    public UserTerms(Long userId, TermType termType, String termsVer, LocalDateTime agreedDate, boolean isEssential, String lastConsentIp) {
        this.userId = userId;
        this.termType = termType;
        this.termsVer = termsVer;
        this.agreedDate = agreedDate;
        this.isEssential = isEssential;
        this.lastConsentIp = lastConsentIp;
    }
}
