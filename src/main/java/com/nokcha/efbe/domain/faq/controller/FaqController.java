package com.nokcha.efbe.domain.faq.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.faq.dto.response.FaqRspDto;
import com.nokcha.efbe.domain.faq.service.FaqService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "FAQ", description = "고객지원 — 도움말/FAQ")
@RestController
@RequestMapping("/v1/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    // 활성 FAQ 목록 — category 미지정/all 이면 전체, 아니면 해당 카테고리만 (display_order ASC)
    @Operation(summary = "FAQ 목록 조회",
            description = "활성 FAQ 를 display_order 오름차순으로 반환. category 파라미터로 필터링 가능 — " +
                    "ACCOUNT/MATCHING/MESSAGE/PAYMENT/REPORT/ETC, 또는 미지정/all 시 전체.")
    @GetMapping
    public ResponseEntity<RspTemplate<List<FaqRspDto>>> getFaqs(
            @Parameter(description = "카테고리 키 — ACCOUNT/MATCHING/MESSAGE/PAYMENT/REPORT/ETC, 미지정 또는 'all' 이면 전체.")
            @RequestParam(required = false) String category) {
        List<FaqRspDto> data = faqService.getFaqs(category);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "FAQ 목록 조회 성공", data));
    }

    // 인기 FAQ — is_popular=true 만
    @Operation(summary = "인기 FAQ 조회",
            description = "is_popular=true 인 활성 FAQ 만 display_order 오름차순으로 반환")
    @GetMapping("/popular")
    public ResponseEntity<RspTemplate<List<FaqRspDto>>> getPopularFaqs() {
        List<FaqRspDto> data = faqService.getPopularFaqs();
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "인기 FAQ 조회 성공", data));
    }
}
