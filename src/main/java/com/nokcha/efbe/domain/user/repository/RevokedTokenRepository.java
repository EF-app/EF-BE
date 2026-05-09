package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    boolean existsByJti(String jti);

    // 만료된 row 청소 (정기 배치)
    @Modifying
    @Query("delete from RevokedToken r where r.expiresAt < :now")
    int deleteAllExpired(@Param("now") LocalDateTime now);
}
