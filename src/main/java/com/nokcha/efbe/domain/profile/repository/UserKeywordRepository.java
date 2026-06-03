package com.nokcha.efbe.domain.profile.repository;

import com.nokcha.efbe.domain.profile.entity.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {

    // 어드민 유저 상세 — 유저의 관심사 키워드
    List<UserKeyword> findByUserId(Long userId);

    // 섹션 수정 — 유저의 키워드 전체 삭제 후 재삽입 (전체 교체 패턴)
    @Modifying
    @Query("delete from UserKeyword uk where uk.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
