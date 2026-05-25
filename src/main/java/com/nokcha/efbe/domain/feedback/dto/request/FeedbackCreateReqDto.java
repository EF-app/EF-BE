package com.nokcha.efbe.domain.feedback.dto.request;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "피드백 등록 요청")
public class FeedbackCreateReqDto {

    @Schema(description = "피드백 유형", example = "BUG")
    private FeedbackType feedbackType;

    @Schema(description = "피드백 카테고리", example = "UI_BROKEN")
    private FeedbackCategoryCode categoryCode;

    @Schema(description = "요약 제목", example = "프로필 이미지가 깨져서 보여요", maxLength = 200)
    private String title;

    @Schema(description = "상세 내용", example = "프로필 화면 진입 시 이미지가 회색 박스로 보입니다.")
    private String content;

    @Schema(description = "앱 버전 (옵션)", example = "1.2.3", maxLength = 30)
    private String appVersion;

    @Schema(description = "디바이스 정보 — 모델·OS 버전 (옵션)", example = "iPhone 15 Pro / iOS 18.1", maxLength = 200)
    private String deviceInfo;

    @Schema(description = "네트워크 타입 (옵션) — WIFI/4G/5G/UNKNOWN 등", example = "WIFI", maxLength = 20)
    private String networkType;

    public void validate() {

        if (feedbackType == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (categoryCode == null) {
            throw new BusinessException(ErrorCode.INVALID_FEEDBACK_CATEGORY);
        }

        if (title == null || title.isBlank() || title.length() > 200) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (appVersion != null && appVersion.length() > 30) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (deviceInfo != null && deviceInfo.length() > 200) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (networkType != null && networkType.length() > 20) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}
