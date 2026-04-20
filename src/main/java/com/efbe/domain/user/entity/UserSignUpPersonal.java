package com.efbe.domain.user.entity;

import com.efbe.common.entity.BaseEntity;
import com.efbe.domain.profile.entity.UserPersonalType;
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
@Table(name = "user_signup_personal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpPersonal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long signUpSessionId;

    @Column(nullable = false)
    private Long personalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserPersonalType personalType;

    @Builder
    public UserSignUpPersonal(Long signUpSessionId, Long personalId, UserPersonalType personalType) {
        this.signUpSessionId = signUpSessionId;
        this.personalId = personalId;
        this.personalType = personalType;
    }
}
