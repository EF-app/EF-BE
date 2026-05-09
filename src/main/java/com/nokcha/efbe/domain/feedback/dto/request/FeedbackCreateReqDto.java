package com.nokcha.efbe.domain.feedback.dto.request;

import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 피드백(버그신고/기능요청) 등록 요청 — multipart/form-data 의 필드별 @RequestPart 로 받은 값 컨테이너.
// (회원가입 프로필 패턴 동일 — 컨트롤러에서 평문 파트를 enum/길이 검증 후 .of(...) 로 생성.)
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "피드백 등록 요청 — 버그신고/기능요청. multipart/form-data 의 필드별 파트 컨테이너")
public class FeedbackCreateReqDto {

    @Schema(description = "피드백 유형", example = "BUG", requiredMode = Schema.RequiredMode.REQUIRED)
    private FeedbackType feedbackType;

    @Schema(description = "피드백 카테고리 (feedbackType 별 허용 목록 참고)", example = "UI_BROKEN", requiredMode = Schema.RequiredMode.REQUIRED)
    private FeedbackCategoryCode categoryCode;

    @Schema(description = "요약 제목", example = "프로필 이미지가 깨져서 보여요", maxLength = 200, requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "상세 내용", example = "프로필 화면 진입 시 이미지가 회색 박스로 보입니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "앱 버전 (옵션)", example = "1.2.3", maxLength = 30)
    private String appVersion;

    @Schema(description = "디바이스 정보 — 모델·OS 버전 (옵션)", example = "iPhone 15 Pro / iOS 18.1", maxLength = 200)
    private String deviceInfo;

    @Schema(description = "네트워크 타입 (옵션) — WIFI/4G/5G/UNKNOWN 등", example = "WIFI", maxLength = 20)
    private String networkType;

    public static FeedbackCreateReqDto of(
            FeedbackType feedbackType,
            FeedbackCategoryCode categoryCode,
            String title,
            String content,
            String appVersion,
            String deviceInfo,
            String networkType) {
        return new FeedbackCreateReqDto(
                feedbackType, categoryCode, title, content, appVersion, deviceInfo, networkType);
    }
}
