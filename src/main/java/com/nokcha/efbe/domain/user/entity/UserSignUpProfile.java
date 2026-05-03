package com.nokcha.efbe.domain.user.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.profile.converter.IdealPointTypeListConverter;
import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.Mbti;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Job job;

    @Convert(converter = IdealPointTypeListConverter.class)
    @Column(length = 1000)
    private List<IdealPointType> idealPointTypes;

    @Column(length = 255)
    private String message;

    @Builder
    public UserSignUpProfile(Long signUpSessionId, Mbti mbti, Job job, List<IdealPointType> idealPointTypes, String message) {
        this.signUpSessionId = signUpSessionId;
        this.mbti = mbti;
        this.job = job;
        this.idealPointTypes = idealPointTypes;
        this.message = message;
    }

    public void updateAboutMe(Job job, Mbti mbti) {
        this.job = job;
        this.mbti = mbti;
    }

    public void updateIdealPointTypes(List<IdealPointType> idealPointTypes) {
        this.idealPointTypes = idealPointTypes;
    }

    public void updateMessage(String message) {
        this.message = message;
    }
}
