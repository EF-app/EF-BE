package com.efbe.domain.user.entity;

import com.efbe.common.entity.BaseEntity;
import com.efbe.domain.profile.entity.Mbti;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Mbti mbti;

    @Column(nullable = false, length = 255)
    private String message;

    @Builder
    public UserSignUpProfile(Long signUpSessionId, Mbti mbti, String message) {
        this.signUpSessionId = signUpSessionId;
        this.mbti = mbti;
        this.message = message;
    }

    public void updateMessage(String message) {
        this.message = message;
    }
}
