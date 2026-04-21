package com.nokcha.efbe.domain.user.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_signup_custom_interest")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpCustomInterest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long signUpSessionId;

    @Column(nullable = false, length = 50)
    private String keyword;

    @Builder
    public UserSignUpCustomInterest(Long signUpSessionId, String keyword) {
        this.signUpSessionId = signUpSessionId;
        this.keyword = keyword;
    }
}
