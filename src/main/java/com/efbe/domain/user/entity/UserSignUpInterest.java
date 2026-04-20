package com.efbe.domain.user.entity;

import com.efbe.common.entity.BaseEntity;
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
@Table(name = "user_signup_interest")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpInterest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long signUpSessionId;

    @Column(nullable = false)
    private Long interestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserSignUpInterestType interestType;

    @Builder
    public UserSignUpInterest(Long signUpSessionId, Long interestId, UserSignUpInterestType interestType) {
        this.signUpSessionId = signUpSessionId;
        this.interestId = interestId;
        this.interestType = interestType;
    }
}
