package com.nokcha.efbe.domain.blockedIdentity.repository;

import com.nokcha.efbe.domain.blockedIdentity.entity.BlockedIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedIdentityRepository extends JpaRepository<BlockedIdentity, Long> {

    // 가입 시 재가입 차단 대조 / 등록 멱등 체크
    boolean existsByDiHash(String diHash);
}
