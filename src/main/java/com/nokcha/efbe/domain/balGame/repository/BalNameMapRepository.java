package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalNameMap;
import com.nokcha.efbe.domain.balGame.entity.BalNameMapId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// 게임별 익명 닉네임 매핑 레포지토리
public interface BalNameMapRepository extends JpaRepository<BalNameMap, BalNameMapId> {

    // 게임 + 유저로 매핑 조회
    Optional<BalNameMap> findByGameIdAndUserId(Long gameId, Long userId);

    // 해당 게임에서 이미 사용된 닉네임 목록 (중복 방지)
    @Query("select m.nickname from BalNameMap m where m.game.id = :gameId")
    List<String> findUsedNicknames(@Param("gameId") Long gameId);
}
