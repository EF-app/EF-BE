package com.nokcha.efbe.common.response;

import java.util.List;

// 커서 기반 페이지 응답
public record CursorPageResponse<T>(
        List<T> items,
        String nextCursor,
        boolean hasMore
) {

    public CursorPageResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }

    // 끝 페이지 응답 (다음 커서 없음)
    public static <T> CursorPageResponse<T> last(List<T> items) {
        return new CursorPageResponse<>(items, null, false);
    }

    // 다음 페이지가 있는 응답
    public static <T> CursorPageResponse<T> of(List<T> items, String nextCursor) {
        return new CursorPageResponse<>(items, nextCursor, true);
    }
}
