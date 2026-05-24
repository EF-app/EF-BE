package com.nokcha.efbe.domain.admin.postIt.dto.response;

import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.entity.PostItColor;
import com.nokcha.efbe.domain.postIt.repository.projection.AdminPostItRow;
import com.nokcha.efbe.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 포스트잇 응답 DTO — 목록/상세 공용. 익명 마스킹 없음 (FE 가 "작성자 보기" 토글로 처리).
@Getter
@Builder
@Schema(description = "어드민 포스트잇 응답 (목록/상세 공용)")
public class AdminPostItRspDto {

    @Schema(description = "포스트잇 PK", example = "42")
    private Long id;

    @Schema(description = "작성자 유저 PK", example = "2")
    private Long userId;

    @Schema(description = "작성자 UUID", example = "7f5a8b2c-4e91-4c33-9a55-9f3b1e0d1234")
    private String userUuid;

    @Schema(description = "작성자 닉네임 (익명 글이어도 노출 — 어드민용)", example = "밤하늘공")
    private String userNickname;

    @Schema(description = "작성자 나이", example = "27")
    private Integer userAge;

    @Schema(description = "작성자 지역 (country + city, 없으면 null)",
            example = "대한민국 서울특별시",
            nullable = true)
    private String userArea;

    @Schema(description = "카테고리 코드", example = "DAILY")
    private PostCategory categoryCode;

    @Schema(description = "본문 (원본, 어드민용이라 마스킹 없음)", example = "오늘은 너무 좋은 날이었어!")
    private String content;

    @Schema(description = "포스트잇 색상", example = "YELLOW")
    private PostItColor color;

    @Schema(description = "익명 게시 여부", example = "false")
    private boolean anonymous;

    @Schema(description = "자동 만료 시각 (null = 만료 없음)",
            example = "2026-06-23T00:00:00",
            nullable = true)
    private LocalDateTime expiresAt;

    @Schema(description = "상단 고정 종료 시각 (null = 미고정)",
            example = "2026-05-25T12:00:00",
            nullable = true)
    private LocalDateTime pinnedUntil;

    @Schema(description = "누적 신고 수 (숨김 해제 시 0 으로 리셋됨)", example = "3")
    private Integer reportCount;

    @Schema(description = "답글 수", example = "12")
    private Integer replyCount;

    @Schema(description = "좋아요 수", example = "47")
    private long likeCount;

    @Schema(description = "관리자에 의해 숨김 처리된 글 여부", example = "false")
    private boolean hidden;

    @Schema(description = "작성자 또는 시스템에 의해 삭제된 글 여부 (soft delete)", example = "false")
    private boolean deleted;

    @Schema(description = "작성 시각", example = "2026-05-20T14:30:00")
    private LocalDateTime createTime;

    @Schema(description = "최종 수정 시각", example = "2026-05-24T09:15:00")
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
