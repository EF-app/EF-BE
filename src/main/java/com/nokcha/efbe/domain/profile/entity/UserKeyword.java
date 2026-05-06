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
        name = "user_keyword",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_keyword_user_id_keyword_id",
                        columnNames = {"user_id", "keyword_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long keywordId;

    @Builder
    public UserKeyword(Long userId, Long keywordId) {
        this.userId = userId;
        this.keywordId = keywordId;
    }
}
