package com.nokcha.efbe.domain.notice.dto.request;

import com.nokcha.efbe.domain.notice.entity.NoticeCategory;
import com.nokcha.efbe.domain.notice.entity.NoticeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공지사항 요청")
public class NoticeReqDto {

    @NotBlank(message = "공지사항 제목은 필수입니다.")
    @Size(max = 100, message = "공지사항 제목은 100자 이하로 입력해야 합니다.")
    @Schema(description = "공지사항 제목", example = "서비스 점검 안내")
    private String title;

    @NotBlank(message = "공지사항 내용은 필수입니다.")
    @Size(max = 2000, message = "공지사항 내용은 2000자 이하로 입력해야 합니다.")
    @Schema(description = "공지사항 내용", example = "2026년 4월 30일 오전 2시부터 4시까지 서비스 점검이 진행됩니다.")
    private String content;

    @Schema(description = "공지사항 카테고리. 미입력 시 NOTICE", example = "UPDATE")
    private NoticeCategory category;

    @Schema(description = "공지사항 상태. 미입력 시 PUBLISHED", example = "DRAFT")
    private NoticeStatus status;

    @Schema(description = "예약 발행 시각. status 가 SCHEDULED 일 때만 사용 (단위: 10분)", example = "2026-05-08T18:00:00")
    private LocalDateTime scheduledAt;
}
