package com.nokcha.efbe.domain.user.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
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

@Getter
@Entity
@Table(name = "user_signup_keyword")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long signUpSessionId;

    @Column(nullable = false)
    private Long keywordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserSignUpKeywordType keywordType;

    @Builder
    public UserSignUpKeyword(Long signUpSessionId, Long keywordId, UserSignUpKeywordType keywordType) {
        this.signUpSessionId = signUpSessionId;
        this.keywordId = keywordId;
        this.keywordType = keywordType;
    }
}
