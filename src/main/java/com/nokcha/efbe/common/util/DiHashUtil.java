package com.nokcha.efbe.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * 본인확인값(DI) 를 재가입 차단 원장 대조용 HMAC-SHA256 해시(hex 64)로 변환.
 *
 * 원문 DI 는 어디에도 저장하지 않고, 키를 아는 서버만 재현 가능한 blind index 로만 보관한다.
 * 시크릿은 환경변수(SPRING_APPLICATION_JSON 등)로 주입 — JWT 시크릿과 동일 운영 패턴.
 *
 * ⚠️ 본인인증(PASS 등) 미도입 상태에서는 hash() 에 넘길 실제 DI 가 없어 호출되지 않는다(전 구간 null 전파).
 *    DI 도입 시 시크릿(security.di-hmac-secret) 설정 + 가입 플로우에서 hash(di) 호출만 붙이면 활성화된다.
 */
@Component
public class DiHashUtil {

    private static final String ALGO = "HmacSHA256";
    private final String secret;

    public DiHashUtil(@Value("${security.di-hmac-secret:}") String secret) {
        this.secret = secret;
    }

    /** DI → HMAC-SHA256 hex. di 가 없으면(본인인증 전) null 반환 → 등록/대조 모두 no-op. */
    public String hash(String di) {
        if (di == null || di.isBlank()) {
            return null;
        }
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("security.di-hmac-secret 미설정 — DI 해시를 생성할 수 없습니다.");
        }
        try {
            Mac mac = Mac.getInstance(ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGO));
            byte[] out = mac.doFinal(di.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("DI 해시 생성 실패", e);
        }
    }
}
