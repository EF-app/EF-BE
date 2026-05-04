package com.nokcha.efbe.domain.profile.entity;

import com.nokcha.efbe.domain.profile.converter.IdealPointTypeListConverter;
import com.nokcha.efbe.domain.user.entity.Job;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "profile")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Mbti mbti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Purpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Job job;

    @Convert(converter = IdealPointTypeListConverter.class)
    @Column(length = 1000)
    private List<IdealPointType> idealPointTypes;

    @Column(length = 300)
    private String message;

    @Builder
    public Profile(Long userId, Mbti mbti, Purpose purpose, Job job, List<IdealPointType> idealPointTypes, String message) {
        this.userId = userId;
        this.mbti = mbti;
        this.purpose = purpose;
        this.job = job;
        this.idealPointTypes = idealPointTypes;
        this.message = message;
    }

    public void update(Mbti mbti, Purpose purpose, Job job, List<IdealPointType> idealPointTypes, String message) {
        this.mbti = mbti;
        this.purpose = purpose;
        this.job = job;
        this.idealPointTypes = idealPointTypes;
        this.message = message;
    }
}