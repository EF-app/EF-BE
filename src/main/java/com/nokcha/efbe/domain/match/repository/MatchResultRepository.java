package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.match.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    /** 페어 (LEAST, GREATEST) 정규화 후 조회. 한 페어당 row 1개 (UNIQUE uk_match_pair). */
    Optional<MatchResult> findByUserAIdAndUserBId(Long userAId, Long userBId);

    boolean existsByUserAIdAndUserBId(Long userAId, Long userBId);
}
