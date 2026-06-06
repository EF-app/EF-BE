package com.nokcha.efbe.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 권한
    INVALID_USER(401, "로그인 정보를 확인해주세요."),
    FORBIDDEN_ROLE(403, "관리자 권한이 필요합니다."),
    // [merge-squash] develop 의존 누락분 보강 — develop 도메인(postIt chat/like, balGame) 사용
    FORBIDDEN(403, "권한이 없습니다."),
    DUPLICATE_LIKE(409, "이미 좋아요를 누른 게시글입니다."),
    MESSAGE_ALREADY_READ(400, "이미 읽음 처리된 메시지입니다."),
    COMMENT_REPLY_DEPTH_EXCEEDED(400, "허용된 댓글 깊이를 초과했습니다."),
    ADMIN_TOKEN_EXPIRED(401, "관리자 토큰이 만료되었습니다."),
    ADMIN_TOKEN_INVALID(401, "유효하지 않은 관리자 토큰입니다."),
    ADMIN_UNAUTHORIZED(401, "관리자 인증이 필요합니다."),
    ADMIN_FORBIDDEN(403, "관리자 권한이 없습니다."),
    ADMIN_LOGIN_FAILED(401, "관리자 아이디 또는 비밀번호가 올바르지 않습니다."),
    ADMIN_NOT_FOUND(404, "존재하지 않는 관리자입니다."),
    ADMIN_ACCOUNT_DISABLED(403, "비활성화된 관리자 계정입니다."),
    ADMIN_ACCOUNT_LOCKED(403, "비밀번호 실패 누적으로 잠긴 계정입니다. 잠시 후 다시 시도해주세요."),

    // 토큰
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(401, "만료된 리프레시 토큰입니다."),

    // 회원가입
    TERMS_AGREEMENT_REQUIRED(400, "필수 약관 동의가 필요합니다."),
    PASSWORD_CONFIRM_MISMATCH(400, "비밀번호 확인이 일치하지 않습니다."),
    EMAIL_CONFIRM_MISMATCH(400, "이메일 확인이 일치하지 않습니다."),
    PHONE_VERIFICATION_REQUIRED(400, "휴대폰 인증이 필요합니다."),
    FEMALE_VERIFICATION_REQUIRED(400, "여성 인증이 필요합니다."),
    ADULT_VERIFICATION_REQUIRED(400, "성인 인증이 필요합니다."),
    CREDENTIALS_REQUIRED(400, "아이디와 비밀번호 입력이 필요합니다."),
    PURPOSE_REQUIRED(400, "가입 목적 선택이 필요합니다."),
    PROFILE_REQUIRED(400, "프로필 정보 입력이 필요합니다."),
    PROFILE_IMAGE_COUNT_EXCEEDED(400, "프로필 이미지는 최대 5장까지 등록할 수 있습니다."),
    INVALID_PROFILE_IMAGE(400, "유효하지 않은 프로필 이미지입니다."),
    INVALID_FEEDBACK_IMAGE(400, "유효하지 않은 피드백 이미지입니다."),
    PERSONAL_REQUIRED(400, "성향 정보는 1개 이상 선택해야 합니다."),
    ALCOHOL_REQUIRED(400, "음주 여부는 필수입니다."),
    SMOKING_REQUIRED(400, "흡연 여부는 필수입니다."),
    INVALID_REGISTRATION_TOKEN(401, "유효하지 않은 회원가입 토큰입니다."),
    EXPIRED_REGISTRATION_TOKEN(401, "만료된 회원가입 토큰입니다."),
    SIGNUP_SESSION_NOT_FOUND(404, "회원가입 정보를 찾을 수 없습니다."),
    INTEREST_NOT_FOUND(404, "존재하지 않는 관심사입니다."),
    PERSONAL_NOT_FOUND(404, "존재하지 않는 성향 정보입니다."),
    INVALID_IDEAL_PERSONAL_CATEGORY(400, "이상형 선택은 머리, 체형, 키, 성향만 가능합니다."),
    INVALID_LOGIN(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    WITHDRAWN_USER(403, "탈퇴한 회원은 로그인할 수 없습니다."),
    ALREADY_WITHDRAWN_USER(409, "이미 탈퇴 처리된 회원입니다."),
    WITHDRAWAL_REQUEST_NOT_FOUND(404, "탈퇴 요청 정보를 찾을 수 없습니다."),
    INVALID_WITHDRAWAL_STATUS(400, "현재 탈퇴 상태에서는 요청을 처리할 수 없습니다."),
    BANNED_USER_SEVEN_DAYS(403, "7일 정지된 회원은 로그인할 수 없습니다."),
    BANNED_USER_THIRTY_DAYS(403, "30일 정지된 회원은 로그인할 수 없습니다."),
    BANNED_USER_FOREVER(403, "영구 정지된 회원은 로그인할 수 없습니다."),
    ALREADY_USER(409, "이미 존재하는 유저입니다."),
    ALREADY_PHONE(409, "이미 가입된 휴대폰 번호입니다."),
    ALREADY_NICKNAME(409, "이미 사용 중인 닉네임입니다."),
    NICKNAME_REQUIRED(400, "닉네임 입력이 필요합니다."),
    AREA_REQUIRED(400, "지역 선택이 필요합니다."),

    // 제재 (suspension)
    NOT_FOUND_SUSPENSION(404, "제재 이력을 찾을 수 없습니다."),
    ALREADY_LIFTED_SUSPENSION(409, "이미 해제된 제재입니다."),
    INVALID_SUSPENSION_REQUEST(400, "유효하지 않은 제재 요청입니다."),
    SUSPENDED_ACCESS_DENIED(403, "제재 상태에서는 이용할 수 없는 기능입니다."),

    // 회원관리
    BLOCKED_USER(403, "차단된 사용자입니다."),
    NOT_FOUND_USER(404, "존재하지 않는 사용자입니다."),
    NOT_FOUND_PROFILE(404, "프로필을 찾을 수 없습니다."),
    SCODE_CONFIRM_MISMATCH(400, "보안코드 확인이 일치하지 않습니다."),
    WRONG_PASSWORD(401, "비밀번호가 일치하지 않습니다."),
    WRONG_SCODE(401, "보안코드가 일치하지 않습니다."),
    SAME_AS_OLD_PASSWORD(400, "기존 비밀번호와 동일합니다."),
    SAME_AS_OLD_SCODE(400, "기존 보안코드와 동일합니다."),

    // 공통 — 페이지·커서·요청
    INVALID_CURSOR(400, "유효하지 않은 페이지 커서입니다."),
    INVALID_PAGE_SIZE(400, "페이지 크기가 유효하지 않습니다."),
    INVALID_REQUEST(400, "유효하지 않은 요청입니다."),
    SELF_ACTION_FORBIDDEN(400, "자기 자신에게는 수행할 수 없는 동작입니다."),
    NOT_FOUND_CATEGORY(404, "존재하지 않는 카테고리입니다."),

    // 매칭 도메인
    DUPLICATE_MATCH_ACTION(409, "이미 처리된 매칭 동작입니다."),
    NOT_FOUND_MATCH_ACTION(404, "매칭 동작을 찾을 수 없습니다."),
    NOT_FOUND_MATCH_RESULT(404, "매칭 결과를 찾을 수 없습니다."),
    DUPLICATE_BLOCK(409, "이미 차단된 사용자입니다."),
    NOT_FOUND_BLOCK(404, "차단 정보를 찾을 수 없습니다."),
    DUPLICATE_INTEREST(409, "이미 등록된 관심사입니다."),
    NOT_FOUND_INTEREST(404, "관심사를 찾을 수 없습니다."),
    NOT_FOUND_IDEAL(404, "이상형 정보를 찾을 수 없습니다."),
    INVALID_IDEAL_RANGE(400, "이상형 범위 값이 유효하지 않습니다."),

    // 채팅 (매칭/포스트잇 공통)
    NOT_FOUND_CHAT_ROOM(404, "채팅방을 찾을 수 없습니다."),
    NOT_FOUND_CHAT_MESSAGE(404, "메시지를 찾을 수 없습니다."),
    CHAT_ROOM_INACTIVE(400, "비활성화된 채팅방입니다."),
    CHAT_NOT_PARTICIPANT(403, "채팅방 참여자가 아닙니다."),

    // 밸런스 게임
    NOT_FOUND_GAME(404, "밸런스 게임을 찾을 수 없습니다."),
    INVALID_GAME_STATUS(400, "현재 게임 상태에서는 수행할 수 없는 동작입니다."),
    GAME_NOT_PUBLISHED(400, "발행되지 않은 게임입니다."),
    BAL_GAME_ALREADY_ARCHIVED(400, "종료(ARCHIVED) 된 밸런스 게임은 변경할 수 없습니다."),
    BAL_GAME_CONTENT_LOCKED(400, "게시 중인 밸런스 게임은 내용을 변경할 수 없습니다."),
    BAL_GAME_INVALID_TRANSITION(400, "허용되지 않는 상태 전환입니다."),
    BAL_GAME_INVALID_SCHEDULE(400, "예약/종료 시각이 유효하지 않습니다."),
    NOT_FOUND_APPLY(404, "신청 정보를 찾을 수 없습니다."),
    NOT_FOUND_VOTE(404, "투표를 찾을 수 없습니다."),
    DUPLICATE_VOTE(409, "이미 투표한 게임입니다."),
    SAME_CHOICE_VOTE(400, "동일 선택지로의 변경 투표입니다."),
    NOT_FOUND_COMMENT(404, "댓글을 찾을 수 없습니다."),
    COMMENT_NOT_OWNER(403, "댓글 작성자만 수행할 수 있습니다."),
    NOT_FOUND_LIKE(404, "좋아요 정보를 찾을 수 없습니다."),
    NOT_VOTED_FOR_COMMENT(400, "투표 후 댓글에 참여할 수 있습니다."),
    NICKNAME_GENERATION_FAILED(500, "닉네임 생성에 실패했습니다."),
    NICKNAME_ALREADY_TAKEN(409, "이미 사용 중인 닉네임입니다."),
    NICKNAME_COOLDOWN(429, "닉네임 변경 쿨다운 기간입니다."),

    // 포스트잇
    NOT_FOUND_POST(404, "포스트잇을 찾을 수 없습니다."),
    POST_NOT_OWNER(403, "포스트 작성자만 수행할 수 있습니다."),
    POST_EXPIRED(400, "만료된 포스트입니다."),
    POST_LIGHTNING_ANONYMOUS(400, "번개 포스트는 닉네임으로만 작성할 수 있습니다."),

    // 약관/정책
    NOT_FOUND_POLICY_DOCUMENT(404, "활성화된 약관을 찾을 수 없습니다."),

    // 결제·별·아이템·구독·광고
    INSUFFICIENT_STAR(400, "잉크가 부족합니다."),
    INSUFFICIENT_ITEM(400, "아이템 보유 수량이 부족합니다."),
    NOT_FOUND_ITEM(404, "아이템을 찾을 수 없습니다."),
    NOT_FOUND_PLAN(404, "구독 플랜을 찾을 수 없습니다."),
    DUPLICATE_PAYMENT(409, "이미 처리된 결제입니다."),
    DUPLICATE_AD_REWARD(409, "이미 지급된 광고 보상입니다."),
    DAILY_LIMIT_EXCEEDED(429, "일일 한도를 초과했습니다."),

    // 신고·동의·사진
    DUPLICATE_REPORT(409, "이미 신고된 항목입니다."),
    NOT_FOUND_REPORT(404, "신고를 찾을 수 없습니다."),
    REPORT_ALREADY_PROCESSED(409, "이미 처리된 신고입니다."),
    NOT_FOUND_CONSENT(404, "동의 정보를 찾을 수 없습니다."),
    NOT_FOUND_PHOTO(404, "사진을 찾을 수 없습니다."),

    // 공지사항
    NOT_FOUND_NOTICE(404, "존재하지 않는 공지사항입니다."),
    INVALID_SCHEDULED_AT(400, "예약 시각이 유효하지 않습니다."),
    ORIGINAL_NOTICE_REQUIRED(400, "정정 공지는 원본 공지 ID가 필요합니다."),
    INVALID_AMEND_NOTICE_UPDATE(400, "정정 공지는 수정으로 전환할 수 없습니다."),

    // 피드백 (버그신고/기능요청)
    INVALID_FEEDBACK_CATEGORY(400, "선택한 카테고리는 해당 피드백 유형에서 사용할 수 없습니다."),
    NOT_FOUND_FEEDBACK(404, "존재하지 않는 피드백입니다."),

    // 매칭 액션
    MATCH_ACTION_SELF(400, "자기 자신에게는 액션을 수행할 수 없습니다."),
    MATCH_ACTION_TARGET_NOT_FOUND(404, "대상 유저를 찾을 수 없습니다.");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
