package com.nokcha.efbe.domain.admin.postIt.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 어드민 포스트잇 숨김 요청 — reason 선택.
// reason 은 추후 audit_log 테이블 연결 시 사용. 현 단계는 로그 출력만.
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class AdminPostItHideReqDto {
    private String reason;
}
