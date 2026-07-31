package com.nokcha.efbe.domain.admin.match.controller;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchFullBatchRspDto;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchRecoverBatchRspDto;
import com.nokcha.efbe.domain.admin.match.service.AdminMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 외부 스케줄러(Cloud Scheduler 등) 전용 야간 배치 트리거.
 *
 *  Cloud Run 은 유휴 시 인스턴스가 0 으로 축소되어 앱 내부 {@code @Scheduled} 가 동작하지 않는다.
 *  따라서 04:00/05:00 배치를 Cloud Scheduler 가 이 HTTP 엔드포인트로 트리거하고,
 *  요청이 처리되는 동안 인스턴스가 살아있어 배치가 동기 완료된다.
 *
 *  보안 — JWT 대신 공유 시크릿 헤더({@code X-Batch-Token})로 보호.
 *    - SecurityConfig 에서 {@code /v1/internal/batch/**} 는 permitAll (JWT 우회)
 *    - 실제 보호는 이 컨트롤러의 토큰 검증. 토큰 미설정 시 fail-closed(전부 차단).
 *    - 내부 동작은 관리자 수동 트리거와 동일 본문({@link AdminMatchService})을 재사용.
 */
@Tag(name = "Internal Batch", description = "외부 스케줄러 전용 배치 트리거 (X-Batch-Token 헤더 필수)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/internal/batch")
public class InternalBatchController {

    private static final String TOKEN_HEADER = "X-Batch-Token";

    private final AdminMatchService adminMatchService;

    @Value("${batch.trigger-token:}")
    private String triggerToken;

    @Operation(summary = "야간 정상 배치 트리거 (04:00 cron 과 동일)",
            description = "활성 viewer 전체 daily_feed 재계산. Cloud Scheduler 가 매일 04:00 KST 호출. " +
                    "동기 실행 — 배치 완료까지 응답 지연됨. ShedLock 우회(단일 호출 보장은 스케줄러 책임).")
    @PostMapping("/nightly-full")
    public RspTemplate<AdminMatchFullBatchRspDto> triggerFull(
            @RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        verify(token);
        return new RspTemplate<>(HttpStatus.OK, "정상 배치 실행 완료", adminMatchService.runFullBatch());
    }

    @Operation(summary = "야간 보정 배치 트리거 (05:00 cron 과 동일)",
            description = "오늘 daily_feed row 가 없는 활성 viewer 만 재시도. Cloud Scheduler 가 매일 05:00 KST 호출. " +
                    "idempotent — 중복 호출되어도 안전.")
    @PostMapping("/nightly-recover")
    public RspTemplate<AdminMatchRecoverBatchRspDto> triggerRecover(
            @RequestHeader(value = TOKEN_HEADER, required = false) String token) {
        verify(token);
        return new RspTemplate<>(HttpStatus.OK, "보정 배치 실행 완료", adminMatchService.runRecoverBatch());
    }

    /**
     * 공유 시크릿 검증. 상수 시간 비교(타이밍 공격 방지).
     *  - 서버에 토큰 미설정 → fail-closed 로 전부 차단(실수로 열린 엔드포인트 방지).
     *  - 헤더 누락/불일치 → 403.
     */
    private void verify(String token) {
        if (triggerToken == null || triggerToken.isBlank()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "배치 트리거 토큰이 서버에 설정되지 않았습니다.");
        }
        byte[] expected = triggerToken.getBytes(StandardCharsets.UTF_8);
        byte[] actual = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "유효하지 않은 배치 트리거 토큰입니다.");
        }
    }
}
