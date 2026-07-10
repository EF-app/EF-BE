package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.CodeItem;
import com.nokcha.efbe.domain.payment.model.ItemResetPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "아이템 사용 한도 잔여 정보 (COUNT 아이템 전용)")
public class ItemQuotaRspDto {

    @Schema(description = "아이템 코드", example = "post_write")
    private String itemCode;

    @Schema(description = "표시 이름", example = "포스트잇 글쓰기")
    private String name;

    @Schema(description = "현재 등급 기준 한도 (-1 = 무제한)", example = "50")
    private int limit;

    @Schema(description = "현재 주기 사용 횟수", example = "3")
    private int used;

    @Schema(description = "남은 횟수. 무제한이면 null.", example = "47", nullable = true)
    private Integer remaining;

    @Schema(description = "무제한 여부 (프리미엄 등)", example = "false")
    private boolean unlimited;

    @Schema(description = "리셋 주기 — DAILY / WEEKLY / MONTHLY / NONE", example = "DAILY")
    private ItemResetPeriod resetPeriod;

    @Schema(description = "현재 주기 키 (DAILY=yyyy-MM-dd / MONTHLY=yyyy-MM)", example = "2026-07-09")
    private String periodKey;

    public static ItemQuotaRspDto of(CodeItem item, int limit, int used,
                                     Integer remaining, boolean unlimited, String periodKey) {
        return ItemQuotaRspDto.builder()
                .itemCode(item.getItemCode())
                .name(item.getName())
                .limit(limit)
                .used(used)
                .remaining(remaining)
                .unlimited(unlimited)
                .resetPeriod(item.getResetPeriod())
                .periodKey(periodKey)
                .build();
    }
}
