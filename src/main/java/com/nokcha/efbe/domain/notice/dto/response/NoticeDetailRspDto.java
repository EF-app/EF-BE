package com.nokcha.efbe.domain.notice.dto.response;

import com.nokcha.efbe.domain.notice.entity.NoticeCategory;
import com.nokcha.efbe.domain.notice.entity.Notice;
import com.nokcha.efbe.domain.notice.entity.NoticeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "공지사항 상세 응답")
public class NoticeDetailRspDto {

    @Schema(description = "공지사항 ID", example = "1")
    private Long id;

    @Schema(description = "공지사항 제목", example = "서비스 점검 안내")
    private String title;

    @Schema(description = "공지사항 작성자 닉네임", example = "관리자")
    private String author;

    @Schema(description = "공지사항 내용", example = "2026년 4월 30일 오전 2시부터 4시까지 서비스 점검이 진행됩니다.")
    private String content;

    @Schema(description = "공지사항 카테고리", example = "NOTICE")
    private NoticeCategory category;

    @Schema(description = "정정 공지의 원본 공지 ID", example = "12")
    private Long originalNoticeId;

    @Schema(description = "공지사항 작성일시", example = "2026-04-23T12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "공지사항 조회수", example = "11")
    private Long viewCount;

    @Schema(description = "공지사항 상태", example = "PUBLISHED")
    private NoticeStatus status;

    @Schema(description = "예약 발행 시각", example = "2026-05-08T18:00:00")
    private LocalDateTime scheduledAt;

    @Schema(description = "실제 발행 시각", example = "2026-05-08T18:00:00")
    private LocalDateTime publishedAt;

    @Schema(description = "고정 공지 순서. null 이면 일반 공지", example = "2")
    private Integer sortOrder;

    public static NoticeDetailRspDto from(Notice notice, String authorNickname) {
        return NoticeDetailRspDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .author(authorNickname)
                .content(notice.getContent())
                .category(notice.getCategory())
                .originalNoticeId(notice.getOriginalNoticeId())
                .createTime(notice.getCreateTime())
                .viewCount(notice.getViewCount())
                .status(notice.getStatus())
                .scheduledAt(notice.getScheduledAt())
                .publishedAt(notice.getPublishedAt())
                .sortOrder(notice.getSortOrder())
                .build();
    }
}
