package com.efbe.domain.profile.entity;

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
@Table(name = "profile_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long signUpSessionId;

    @Column
    private Long userId;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false, length = 255)
    private String storedName;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false, length = 500)
    private String url;

    // 회원 프로필 이미지로 변경
    public void assignToUser(Long userId) {
        this.userId = userId;
        this.signUpSessionId = null;
    }

    @Builder
    public ProfileImage(Long signUpSessionId, Long userId, String originalName, String storedName, Integer sortOrder, String url) {
        this.signUpSessionId = signUpSessionId;
        this.userId = userId;
        this.originalName = originalName;
        this.storedName = storedName;
        this.sortOrder = sortOrder;
        this.url = url;
    }
}
