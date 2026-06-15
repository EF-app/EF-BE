package com.nokcha.efbe.domain.match.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * match_daily_feed 엔티티 — 뷰어당/날짜당 50 row.
 *  - 자정 리셋: 배치가 그날 row 를 교체.
 *  - PK: (feed_date, viewer_id, match_rank)
 *
 *  create/update 시각·사용자는 {@link BaseEntity} 의 audit 4 필드로 자동 관리.
 *  slot_type: SCORE / NEWBIE / RANDOM / CUSTOM_KW / FRESH_NEWBIE (ENUM)
 */
@Getter
@Entity
@Table(
        name = "match_daily_feed",
        indexes = {
                @Index(name = "idx_feed_viewer", columnList = "viewer_id, feed_date"),
                // 관리자 일일 피드 조회 — viewer_id 없이 feed_date 만 (default = 오늘) 검색 시 풀 스캔 방지.
                @Index(name = "idx_feed_date", columnList = "feed_date"),
                // 관리자 일일 피드 조회 — target_id 단독/결합 검색 빠르게 + FK lookup 안정성.
                @Index(name = "idx_feed_target", columnList = "target_id")
        }
)
@IdClass(MatchDailyFeed.PK.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchDailyFeed extends BaseEntity {

    @Id
    @Column(name = "feed_date", nullable = false)
    private LocalDate feedDate;

    @Id
    @Column(name = "viewer_id", nullable = false)
    private Long viewerId;

    @Id
    @Column(name = "match_rank", nullable = false)
    private Short matchRank;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "sort_key", nullable = false, precision = 5, scale = 4)
    private BigDecimal sortKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false, length = 16)
    private SlotType slotType;

    @Column(name = "tags_json", nullable = false, columnDefinition = "JSON")
    private String tagsJson;

    @Builder
    private MatchDailyFeed(LocalDate feedDate, Long viewerId, Short matchRank, Long targetId,
                           BigDecimal sortKey, SlotType slotType, String tagsJson) {
        this.feedDate = feedDate;
        this.viewerId = viewerId;
        this.matchRank = matchRank;
        this.targetId = targetId;
        this.sortKey = sortKey;
        this.slotType = slotType;
        this.tagsJson = tagsJson;
    }

    public enum SlotType { SCORE, NEWBIE, RANDOM, CUSTOM_KW, FRESH_NEWBIE }

    /** 복합 PK. */
    @Getter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private static final long serialVersionUID = 1L;
        private LocalDate feedDate;
        private Long viewerId;
        private Short matchRank;

        public PK(LocalDate feedDate, Long viewerId, Short matchRank) {
            this.feedDate = feedDate;
            this.viewerId = viewerId;
            this.matchRank = matchRank;
        }
    }
}
