package com.nokcha.efbe.domain.admin.postIt.dto.response;

import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.entity.PostItColor;
import com.nokcha.efbe.domain.postIt.repository.projection.AdminPostItRow;
import com.nokcha.efbe.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 포스트잇 응답 DTO.
//   - 익명 마스킹 없음 (어드민용, FE 가 "작성자 보기" 토글로 처리)
@Getter
@Builder
public class AdminPostItRspDto {

    private Long id;
    private Long userId;
    private String userUuid;
    private String userNickname;
    private Integer userAge;
    private String userArea;
    private PostCategory categoryCode;
    private String content;
    private PostItColor color;
    private boolean anonymous;
    private LocalDateTime expiresAt;
    private LocalDateTime pinnedUntil;
    private Integer reportCount;
    private Integer replyCount;
    private long likeCount;
    private boolean hidden;
    private boolean deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Querydsl projection 기반 — 목록 표준.
    public static AdminPostItRspDto from(AdminPostItRow r) {
        return AdminPostItRspDto.builder()
                .id(r.id())
                .userId(r.userId())
                .userUuid(r.userUuid())
                .userNickname(r.userNickname())
                .userAge(r.userAge())
                .userArea(composeLocation(r.areaCountry(), r.areaCity()))
                .categoryCode(r.categoryCode())
                .content(r.content())
                .color(r.color())
                .anonymous(Boolean.TRUE.equals(r.isAnonymous()))
                .expiresAt(r.expiresAt())
                .pinnedUntil(r.pinnedUntil())
                .reportCount(r.reportCount() == null ? 0 : r.reportCount())
                .replyCount(r.replyCount() == null ? 0 : r.replyCount())
                .likeCount(r.likeCount() == null ? 0L : r.likeCount())
                .hidden(Boolean.TRUE.equals(r.isHidden()))
                .deleted(Boolean.TRUE.equals(r.isDeleted()))
                .createTime(r.createTime())
                .updateTime(r.updateTime())
                .build();
    }

    // 단건 조회
    public static AdminPostItRspDto from(PostIt p, long likeCount, CodeArea area) {
        User user = p.getUser();
        return AdminPostItRspDto.builder()
                .id(p.getId())
                .userId(user == null ? null : user.getId())
                .userUuid(user == null ? null : user.getUuid())
                .userNickname(user == null ? null : user.getNickname())
                .userAge(user == null ? null : user.getAge())
                .userArea(area == null ? null : composeLocation(area.getCountry(), area.getCity()))
                .categoryCode(p.getCategoryCode())
                .content(p.getContent())
                .color(p.getColor())
                .anonymous(Boolean.TRUE.equals(p.getIsAnonymous()))
                .expiresAt(p.getExpiresAt())
                .pinnedUntil(p.getPinnedUntil())
                .reportCount(p.getReportCount() == null ? 0 : p.getReportCount())
                .replyCount(p.getReplyCount() == null ? 0 : p.getReplyCount())
                .likeCount(likeCount)
                .hidden(Boolean.TRUE.equals(p.getIsHidden()))
                .deleted(Boolean.TRUE.equals(p.getIsDeleted()))
                .createTime(p.getCreateTime())
                .updateTime(p.getUpdateTime())
                .build();
    }

    private static String composeLocation(String country, String city) {
        boolean hasCountry = country != null && !country.isBlank();
        boolean hasCity = city != null && !city.isBlank();
        if (!hasCountry && !hasCity) return null;
        if (hasCountry && hasCity) return country + " " + city;
        return hasCountry ? country : city;
    }
}
