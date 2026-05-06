package com.nokcha.efbe.domain.operation.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.operation.dto.response.PolicyDocumentDetailRspDto;
import com.nokcha.efbe.domain.operation.dto.response.PolicyDocumentSummaryRspDto;
import com.nokcha.efbe.domain.operation.service.PolicyDocumentService;
import com.nokcha.efbe.domain.user.entity.TermType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Tag(name = "Policy Document", description = "약관/정책 본문 조회 API (회원가입 전 단계 노출)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/policies")
public class PolicyDocumentController {

    private final PolicyDocumentService policyDocumentService;

    @Operation(summary = "활성 약관 목록", description = "회원가입 약관 동의 화면용. 본문(content) 미포함, 목록만 가벼운 응답. ETag 매칭 시 304 응답.")
    @GetMapping
    public ResponseEntity<RspTemplate<List<PolicyDocumentSummaryRspDto>>> getActivePolicies(WebRequest webRequest) {
        List<PolicyDocumentSummaryRspDto> data = policyDocumentService.getActivePolicies();

        // 어느 한 정책이라도 버전이 바뀌면 ETag 변경
        String etag = "\"policies-" + data.stream()
                .map(p -> p.getPolicyType().name() + ":" + p.getVersion())
                .collect(Collectors.joining(",")) + "\"";

        if (webRequest.checkNotModified(etag)) {
            return null;
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(new RspTemplate<>(HttpStatus.OK, "활성 약관 목록 조회 성공", data));
    }

    @Operation(summary = "약관 상세", description = "전문 보기 모달용. policyType 의 최신 활성 버전 본문 전체 반환. ETag 매칭 시 304 응답.")
    @GetMapping("/{policyType}")
    public ResponseEntity<RspTemplate<PolicyDocumentDetailRspDto>> getPolicyDetail(
            @PathVariable TermType policyType,
            WebRequest webRequest) {
        PolicyDocumentDetailRspDto data = policyDocumentService.getPolicyDetail(policyType);

        String etag = "\"" + policyType.name() + "-" + data.getVersion() + "\"";

        if (webRequest.checkNotModified(etag)) {
            return null;
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(new RspTemplate<>(HttpStatus.OK, "약관 상세 조회 성공", data));
    }
}
