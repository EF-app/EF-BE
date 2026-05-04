package com.nokcha.efbe.common.util;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;

// 커서 페이지네이션 인코딩/디코딩 유틸
// 커서 = Base64URL( JSON(payload) ) — 도메인별 payload 형태(record)는 호출 측이 정의
// 예) MatchPoolCursor(score, targetId), ChatCursor(lastSentAt, roomId)
@Component
@RequiredArgsConstructor
public class CursorCodec {

    private final ObjectMapper objectMapper;

    // payload → 커서 문자열 (URL safe Base64, 패딩 없음)
    public String encode(Object payload) {
        if (payload == null) return null;
        try {
            byte[] json = objectMapper.writeValueAsBytes(payload);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("cursor encode failed", e);
        }
    }

    // 커서 문자열 → payload (null·blank → null 반환)
    public <T> T decode(String cursor, Class<T> type) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            byte[] json = Base64.getUrlDecoder().decode(cursor);
            return objectMapper.readValue(json, type);
        } catch (IllegalArgumentException | java.io.IOException e) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
    }
}
