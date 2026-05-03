package com.nokcha.efbe.domain.profile.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_custom_interest",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_custom_interest_user_id_normalized_keyword",
                        columnNames = {"user_id", "normalized_keyword"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCustomInterest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String keyword;

    @Column(nullable = false, length = 50)
    private String normalizedKeyword;

    @Builder
    public UserCustomInterest(Long userId, String keyword, String normalizedKeyword) {
        this.userId = userId;
        this.keyword = keyword;
        this.normalizedKeyword = normalizedKeyword;
    }
}
