package com.nokcha.efbe.domain.profile.entity;

import com.nokcha.efbe.domain.profile.converter.IdealPointTypeListConverter;
import com.nokcha.efbe.domain.user.entity.Job;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_profile")
public class UserProfile {

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
    private String bioMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) NOT NULL DEFAULT 'APPROVED'")
    private ProfileStatus profileStatus;

    @Column(name = "profile_rejected_reason")
    private String profileRejectedReason;

    @Column(name = "profile_reviewed_at")
    private LocalDateTime profileReviewedAt;

    @Column(name = "profile_reviewed_by")
    private Long profileReviewedBy;

    @Builder
    public UserProfile(Long userId, Mbti mbti, Purpose purpose, Job job, List<IdealPointType> idealPointTypes, String bioMessage) {
        this.userId = userId;
        this.mbti = mbti;
        this.purpose = purpose;
        this.job = job;
        this.idealPointTypes = idealPointTypes;
        this.bioMessage = bioMessage;
        this.profileStatus = ProfileStatus.APPROVED;
    }

    public void update(Mbti mbti, Purpose purpose, Job job, List<IdealPointType> idealPointTypes, String message) {
        this.mbti = mbti;
        this.purpose = purpose;
        this.job = job;
        this.idealPointTypes = idealPointTypes;
        this.bioMessage = message;
    }

    // 섹션별 부분 업데이트 (My 프로필 수정 화면)
    public void updateMbti(Mbti mbti) {
        this.mbti = mbti;
    }

    public void updateBio(String bioMessage) {
        this.bioMessage = bioMessage;
    }

    public void updatePurpose(Purpose purpose) {
        this.purpose = purpose;
    }

    public void updateJob(Job job) {
        this.job = job;
    }

    public void updateIdealPointTypes(List<IdealPointType> idealPointTypes) {
        this.idealPointTypes = idealPointTypes;
    }

    // 관리자 승인
    public void approve(Long reviewerAdminId) {
        this.profileStatus = ProfileStatus.APPROVED;
        this.profileRejectedReason = null;
        this.profileReviewedAt = LocalDateTime.now();
        this.profileReviewedBy = reviewerAdminId;
    }

    // 관리자 반려
    public void reject(String reason, Long reviewerAdminId) {
        this.profileStatus = ProfileStatus.REJECTED;
        this.profileRejectedReason = reason;
        this.profileReviewedAt = LocalDateTime.now();
        this.profileReviewedBy = reviewerAdminId;
    }
}