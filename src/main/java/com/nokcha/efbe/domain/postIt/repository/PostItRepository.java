package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.postIt.entity.PostIt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostItRepository extends JpaRepository<PostIt, Long>, PostItQueryRepository {

    // 만료 임박 배치 (만료 시각이 지난 활성 글)
    @Query("select p from PostIt p where p.expiresAt <= :now and p.isDeleted = false")
    List<PostIt> findExpired(@Param("now") LocalDateTime now);

    // 고정 만료 배치
    @Query("select p from PostIt p where p.pinnedUntil is not null and p.pinnedUntil <= :now")
    List<PostIt> findExpiredPins(@Param("now") LocalDateTime now);
}
