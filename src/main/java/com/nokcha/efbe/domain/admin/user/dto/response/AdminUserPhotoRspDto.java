package com.nokcha.efbe.domain.admin.user.dto.response;

import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 전용 유저 프로필 사진")
public class AdminUserPhotoRspDto {

    @Schema(description = "사진 PK", example = "501")
    private Long id;

    @Schema(description = "사진 URL", example = "https://cdn.ef.com/profile/abc.jpg")
    private String url;

    @Schema(description = "정렬 순서 (0=대표)", example = "0")
    private Integer sortOrder;

    public static AdminUserPhotoRspDto from(UserProfileImage img) {
        return AdminUserPhotoRspDto.builder()
                .id(img.getId())
                .url(img.getUrl())
                .sortOrder(img.getSortOrder())
                .build();
    }
}
