package com.nokcha.efbe.domain.notice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
