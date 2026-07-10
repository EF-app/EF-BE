package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.payment.dto.response.ItemQuotaRspDto;
import com.nokcha.efbe.domain.payment.service.ItemQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Item Quota", description = "아이템 사용 한도 잔여 조회")
@RestController
@RequestMapping("/v1/users/me/items")
@RequiredArgsConstructor
public class ItemQuotaController {

    private final ItemQuotaService itemQuotaService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "아이템 잔여 횟수 조회",
            description = "COUNT 아이템의 현재 주기 남은 횟수. 무제한이면 unlimited=true·remaining=null. COUNT 아닌 코드면 400.")
    @GetMapping("/{itemCode}/quota")
    public RspTemplate<ItemQuotaRspDto> getQuota(@PathVariable String itemCode) {
        Long userId = securityUtil.getCurrentUserId();
        ItemQuotaRspDto data = itemQuotaService.getQuota(userId, itemCode);
        return new RspTemplate<>(HttpStatus.OK, "아이템 잔여 조회 성공", data);
    }
}
