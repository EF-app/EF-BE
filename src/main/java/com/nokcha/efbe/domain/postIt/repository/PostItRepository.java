package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// 포스트잇 레포지토리
// - 단순 CRUD/페이지네이션은 Spring Data JPA
// - 동적 검색·커서·조인 프로젝션은 PostItQueryRepository (Querydsl) 로 위임
public interface PostItRepository extends JpaRepository<PostIt, Long>, PostItQueryRepository {

    // 활성 포스트잇 피드 (숨김/삭제/만료 제외, 상단고정 우선, 최신순)
    @Query("select p from PostIt p " +
            "where p.isHidden = false and p.isDeleted = false and p.expiresAt > :now " +
            "order by case when p.pinnedUntil is not null and p.pinnedUntil > :now then 0 else 1 end, " +
            "p.createTime desc")
    Page<PostIt> findActiveFeed(@Param("now") LocalDateTime now, Pageable pageable);

    // 카테고리 필터 피드
    @Query("select p from PostIt p " +
            "where p.categoryCode = :categoryCode " +
            "and p.isHidden = false and p.isDeleted = false and p.expiresAt > :now " +
            "order by p.createTime desc")
    Page<PostIt> findActiveByCategory(@Param("categoryCode") PostCategory categoryCode,
                                      @Param("now") LocalDateTime now, Pageable pageable);

    // 내가 쓴 글 목록
    Page<PostIt> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    // 만료 임박 배치 (만료 시각이 지난 활성 글)
    @Query("select p from PostIt p where p.expiresAt <= :now and p.isDeleted = false")
    List<PostIt> findExpired(@Param("now") LocalDateTime now);

    // 고정 만료 배치
    @Query("select p from PostIt p where p.pinnedUntil is not null and p.pinnedUntil <= :now")
    List<PostIt> findExpiredPins(@Param("now") LocalDateTime now);
}
