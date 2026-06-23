package com.nokcha.efbe.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "채팅 신고 후 나가기 요청")
public class ChatReportLeaveReqDto {

    @Valid
    @Size(max = 20)
    @Schema(description = "신고 증거 메시지 목록. Firebase 메시지 document id만 전달하면 서버가 Firestore 원본 메시지를 읽어 증거로 저장합니다.")
    private List<MessageEvidence> messages;

    @Getter
    @NoArgsConstructor
    @Schema(description = "채팅 신고 증거 메시지")
    public static class MessageEvidence {

        @NotBlank(message = "Firebase 메시지 document id는 필수입니다.")
        @Schema(description = "Firebase 메시지 document id", example = "9Lpm7VjM3Qe2")
        @Size(max = 200)
        private String firebaseMessageId;
    }
}
