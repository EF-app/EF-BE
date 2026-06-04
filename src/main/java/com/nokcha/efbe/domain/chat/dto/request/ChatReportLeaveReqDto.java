package com.nokcha.efbe.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "채팅 신고 후 나가기 요청")
public class ChatReportLeaveReqDto {

    @Valid
    @Size(max = 20)
    @Schema(description = "신고 증거 메시지 목록. Firebase 메시지 document id와 가능하면 본문 스냅샷을 함께 전달합니다.")
    private List<MessageEvidence> messages;

    @Getter
    @NoArgsConstructor
    @Schema(description = "채팅 신고 증거 메시지")
    public static class MessageEvidence {

        @Schema(description = "Firebase 메시지 document id", example = "9Lpm7VjM3Qe2")
        @Size(max = 200)
        private String firebaseMessageId;

        @Schema(description = "메시지 발신자 유저 ID", example = "12", nullable = true)
        private Long senderUserId;

        @Schema(description = "신고 시점 메시지 본문 스냅샷", example = "욕설 메시지", nullable = true)
        @Size(max = 2000)
        private String contentSnapshot;

        @Schema(description = "Firebase 메시지 전송 시각", example = "2026-06-03T22:10:00", nullable = true)
        private LocalDateTime sentAt;
    }
}
