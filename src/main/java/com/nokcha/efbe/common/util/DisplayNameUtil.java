package com.nokcha.efbe.common.util;

/**
 * 탈퇴 완료(파기) 회원 표시 처리.
 *
 * 파기 시 users.nickname 이 NULL 로 익명화되므로, 다른 유저에게 노출되는 지점(포스트잇·댓글·채팅 등)에서
 * NULL 이면 "탈퇴한 회원" 으로 치환한다.
 *  - WITHDRAWING(30일 유예) 회원은 nickname 이 살아있어(철회 대비) 그대로 실제 닉네임이 노출된다.
 *  - WITHDRAWN(파기 완료) 회원만 nickname==null → "탈퇴한 회원".
 */
public final class DisplayNameUtil {

    public static final String WITHDRAWN_LABEL = "탈퇴한 회원";

    private DisplayNameUtil() {}

    /** 닉네임이 NULL(파기 완료)이면 "탈퇴한 회원", 아니면 원래 닉네임. */
    public static String orWithdrawn(String nickname) {
        return nickname == null ? WITHDRAWN_LABEL : nickname;
    }
}
