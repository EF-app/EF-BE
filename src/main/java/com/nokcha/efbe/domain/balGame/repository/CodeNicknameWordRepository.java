package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.CodeNicknameWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

// 익명 닉네임 단어 사전 레포지토리 (v2.0: type 기반 랜덤 조회)
public interface CodeNicknameWordRepository extends JpaRepository<CodeNicknameWord, Integer> {

    // 활성 형용사 1개 랜덤 조회
    @Query(value = "SELECT word FROM code_nickname_word " +
            "WHERE type = 'ADJ' AND is_active = TRUE " +
            "ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<String> findRandomActiveAdjective();

    // 활성 명사(ANIMAL/FOOD/NATURE) 1개 랜덤 조회
    @Query(value = "SELECT word FROM code_nickname_word " +
            "WHERE type IN ('ANIMAL','FOOD','NATURE') AND is_active = TRUE " +
            "ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<String> findRandomActiveNoun();
}
