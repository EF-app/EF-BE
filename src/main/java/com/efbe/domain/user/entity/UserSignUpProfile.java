package com.efbe.domain.user.entity;

import com.efbe.common.entity.BaseEntity;
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
@Table(name = "user_signup_profile")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long signUpSessionId;

    @Column(nullable = false, length = 255)
    private String message;

    @Builder
    public UserSignUpProfile(Long signUpSessionId, String message) {
        this.signUpSessionId = signUpSessionId;
        this.message = message;
    }

    public void updateMessage(String message) {
        this.message = message;
    }
}
