package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.payment.dto.response.ItemCatalogRspDto;
import com.nokcha.efbe.domain.payment.entity.ItemCategory;
import com.nokcha.efbe.domain.payment.service.ItemCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/items")
@RequiredArgsConstructor
public class ItemCatalogController {

    private final ItemCatalogService itemCatalogService;

    // 활성 아이템 목록 (카테고리 필터)
    @GetMapping
    public RspTemplate<List<ItemCatalogRspDto>> getItems(
            @RequestParam(required = false) ItemCategory category) {
        List<ItemCatalogRspDto> data = itemCatalogService.getItems(category);
        return new RspTemplate<>(HttpStatus.OK, "아이템 목록 조회 성공", data);
    }

    // 아이템 코드 단건 조회
    @GetMapping("/{code}")
    public RspTemplate<ItemCatalogRspDto> getByCode(@PathVariable String code) {
        ItemCatalogRspDto data = itemCatalogService.getByCode(code);
        return new RspTemplate<>(HttpStatus.OK, "아이템 조회 성공", data);
    }
}
