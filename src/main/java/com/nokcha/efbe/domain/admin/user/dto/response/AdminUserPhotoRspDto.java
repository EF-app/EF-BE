package com.nokcha.efbe.domain.admin.user.dto.response;

import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import lombok.Builder;
import lombok.Getter;

// 어드민 유저 상세 — 프로필 사진 한 장.
@Getter
@Builder
public class AdminUserPhotoRspDto {

    private Long id;
    private String url;
    private Integer sortOrder;

    public static AdminUserPhotoRspDto from(UserProfileImage img) {
        return AdminUserPhotoRspDto.builder()
                .id(img.getId())
                .url(img.getUrl())
                .sortOrder(img.getSortOrder())
                .build();
    }
}
