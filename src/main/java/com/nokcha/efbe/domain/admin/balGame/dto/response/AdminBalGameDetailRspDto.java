package com.nokcha.efbe.domain.admin.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 측 밸런스 게임 상세 응답 DTO.
@Getter
@Builder
@Schema(description = "어드민 밸런스 게임 상세")
public class AdminBalGameDetailRspDto {

    @Schema(description = "게임 PK", example = "1")
    private Long id;

    @Schema(description = "외부 노출 식별자 (uuid) — admin API path 호출에 사용")
    private String uuid;

    @Schema(description = "옵션 A 텍스트", example = "교통카드")
    private String optionA;

    @Schema(description = "옵션 A 부연설명")
    private String optionADesc;

    @Schema(description = "옵션 A 이모지", example = "💳")
    private String optionAEmoji;

    @Schema(description = "옵션 B 텍스트", example = "이어폰")
    private String optionB;

    @Schema(description = "옵션 B 부연설명")
    private String optionBDesc;

    @Schema(description = "옵션 B 이모지", example = "🎧")
    private String optionBEmoji;

    @Schema(description = "게임 전체 배경 설명")
    private String description;

    @Schema(description = "카테고리", example = "DAILY")
    private BalCategoryCode categoryCode;

    @Schema(description = "게시 상태", example = "PUBLISHED")
    private BalGameStatus status;

    @Schema(description = "예약 게시 시각")
    private LocalDateTime scheduledAt;

    @Schema(description = "예약 종료 시각")
    private LocalDateTime scheduledEndAt;

    @Schema(description = "총 투표수 (a_count + b_count)", example = "1000")
    private Integer totalCount;

    @Schema(description = "옵션 A 투표수", example = "620")
    private Integer aCount;

    @Schema(description = "옵션 B 투표수", example = "380")
    private Integer bCount;

    @Schema(description = "댓글 총 개수", example = "328")
    private Integer commentCount;

    @Schema(description = "BAL-APPLY 기반 등록인 경우 신청자 user id (자체 등록이면 null)", example = "42")
    private Long applicantUserId;

    @Schema(description = "신청자 닉네임 (탈퇴 시 null)", example = "용감한 다람쥐")
    private String applicantNickname;

    @Schema(description = "투표 통계 (비율 + 연령대/지역 분포) — 단건 상세에서만 채움. createGame/updateGame 응답에서는 null.")
    private AdminBalVoteStatsRspDto voteStats;

    @Schema(description = "최초 등록 시각")
    private LocalDateTime createTime;

    @Schema(description = "마지막 변경 시각")
    private LocalDateTime updateTime;

    // 통계 미주입 변형 — createGame/updateGame 응답에서 사용 (등록/수정 직후엔 통계 의미 없음).
    public static AdminBalGameDetailRspDto from(BalGame g) {
        return from(g, null);
    }

    // 통계 포함 — getGame 응답에서 사용.
    public static AdminBalGameDetailRspDto from(BalGame g, AdminBalVoteStatsRspDto voteStats) {
        int a = g.getACount() == null ? 0 : g.getACount();
        int b = g.getBCount() == null ? 0 : g.getBCount();
        User applicant = g.getApplicant();
        return AdminBalGameDetailRspDto.builder()
                .id(g.getId())
                .uuid(g.getUuid())
                .optionA(g.getOptionA())
                .optionADesc(g.getOptionADesc())
                .optionAEmoji(g.getOptionAEmoji())
                .optionB(g.getOptionB())
                .optionBDesc(g.getOptionBDesc())
                .optionBEmoji(g.getOptionBEmoji())
                .description(g.getDescription())
                .categoryCode(g.getCategoryCode())
                .status(g.getStatus())
                .scheduledAt(g.getScheduledAt())
                .scheduledEndAt(g.getScheduledEndAt())
                .totalCount(a + b)
                .aCount(a)
                .bCount(b)
                .commentCount(g.getCommentCount() == null ? 0 : g.getCommentCount())
                .applicantUserId(applicant == null ? null : applicant.getId())
                .applicantNickname(applicant == null ? null : applicant.getNickname())
                .voteStats(voteStats)
                .createTime(g.getCreateTime())
                .updateTime(g.getUpdateTime())
                .build();
    }
}
