package com.nokcha.efbe.domain.blockedIdentity.service;

import com.nokcha.efbe.domain.blockedIdentity.entity.BlockedIdentity;
import com.nokcha.efbe.domain.blockedIdentity.model.BlockReason;
import com.nokcha.efbe.domain.blockedIdentity.repository.BlockedIdentityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 재가입 차단 원장 등록/대조.
 *
 * di_hash 는 DiHashUtil 로 산출한 HMAC 해시. 본인인증(DI) 미도입 현재는 유저 di_hash 가 전부 null 이라
 * 등록/대조가 모두 no-op — DI 가 채워지는 순간부터 실제 동작한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockedIdentityService {

    private final BlockedIdentityRepository blockedIdentityRepository;

    /** 영구정지자 재가입 차단 등록. di_hash 없으면(본인인증 전) skip, 이미 있으면 멱등 skip. */
    @Transactional
    public void registerPermanentBan(String diHash, Long sourceUserId, String note) {
        if (diHash == null || diHash.isBlank()) {
            return; // 본인인증 미도입 → 차단 근거값 없음
        }
        if (blockedIdentityRepository.existsByDiHash(diHash)) {
            return; // 이미 차단됨
        }
        blockedIdentityRepository.save(BlockedIdentity.builder()
                .diHash(diHash)
                .blockReason(BlockReason.PERMANENT_BAN)
                .sourceUserId(sourceUserId)
                .note(note)
                .build());
        log.info("[BlockedIdentity] 재가입 차단 등록 — sourceUserId={}", sourceUserId);
    }

    /** 가입 시 차단 대조. di_hash 없으면 false(허용). */
    @Transactional(readOnly = true)
    public boolean isBlocked(String diHash) {
        return diHash != null && !diHash.isBlank() && blockedIdentityRepository.existsByDiHash(diHash);
    }
}
