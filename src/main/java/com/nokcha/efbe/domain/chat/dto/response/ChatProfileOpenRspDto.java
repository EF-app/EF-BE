package com.nokcha.efbe.domain.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@Schema(description = "채팅 프로필 공개 응답")
public class ChatProfileOpenRspDto {

    @Schema(description = "채팅방 ID", example = "21")
    private Long chatRoomId;

    @Schema(description = "조회 대상 유저 ID. 익명 채팅이면 null", example = "12", nullable = true)
    private Long targetUserId;

    @Schema(description = "익명 채팅 여부", example = "false")
    private Boolean anonymous;

    @Schema(description = "현재 프로필 공개 단계. 1~4", example = "2")
    private Integer profileOpenLevel;

    @Schema(description = "공개 순서. BASIC은 1단계, FULL은 4단계")
    private List<String> sectionOrder;

    @Schema(description = "기본 공개 프로필")
    private BasicProfile basicProfile;

    @Schema(description = "관심사 키워드. 공개 단계 전이면 null", nullable = true)
    private KeywordProfile keywordProfile;

    @Schema(description = "이상형 정보. 공개 단계 전이면 null", nullable = true)
    private Map<String, List<String>> idealProfile;

    @Schema(description = "전체 프로필. 4단계 전이면 null", nullable = true)
    private FullProfile fullProfile;

    @Getter
    @Builder
    @Schema(description = "기본 공개 프로필")
    public static class BasicProfile {

        @Schema(description = "닉네임. 익명 채팅이면 익명", example = "녹차")
        private String nickname;

        @Schema(description = "한국 나이", example = "25", nullable = true)
        private Integer age;

        @Schema(description = "지역", example = "대한민국 서울특별시", nullable = true)
        private String area;
    }

    @Getter
    @Builder
    @Schema(description = "관심사 키워드 프로필")
    public static class KeywordProfile {

        @Schema(description = "코드 관심사. key=대분류, value=소분류 목록")
        private Map<String, List<String>> keywords;

        @Schema(description = "커스텀 관심사")
        private List<String> customKeywords;
    }

    @Getter
    @Builder
    @Schema(description = "전체 공개 프로필")
    public static class FullProfile {

        @Schema(description = "목적", example = "FRIEND", nullable = true)
        private String purpose;

        @Schema(description = "직업", example = "OFFICE_WORKER", nullable = true)
        private String job;

        @Schema(description = "MBTI", example = "ENFP", nullable = true)
        private String mbti;

        @Schema(description = "한 줄 소개", example = "반가워요", nullable = true)
        private String bioMessage;

        @Schema(description = "나에 대해서. key=대분류, value=소분류 목록")
        private Map<String, List<String>> selfProfile;
    }
}
