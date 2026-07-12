package com.nokcha.efbe.domain.payment.model;

/**
 * code_item 자연키(item_code) 상수 — 도메인 코드가 문자열 하드코딩 대신 참조.
 * 값은 code_item 시드와 1:1.
 */
public final class ItemCodes {

    private ItemCodes() {
    }

    // 포스트잇
    public static final String POST_WRITE = "post_write";
    public static final String POST_FLASH = "post_flash";
    public static final String POST_REPLY = "post_reply";
    public static final String POST_TTL = "post_ttl";
    public static final String POST_PIN = "post_pin";

    // 매칭
    public static final String UNDO = "undo";
    public static final String SUPER_LIKE = "super_like";
    public static final String POWER_MSG = "power_msg";
    public static final String MATCH_LIKE = "match_like";
    public static final String MATCH_BOOST = "match_boost";
    public static final String LIKED_LIST_FULL = "liked_list_full";
    public static final String ONLINE_BADGE = "online_badge";
    public static final String TODAY_PICK = "today_pick";

    // 유저
    public static final String NICKNAME_CHANGE = "nickname_change";
    public static final String LOCATION_CHANGE = "location_change";
}
