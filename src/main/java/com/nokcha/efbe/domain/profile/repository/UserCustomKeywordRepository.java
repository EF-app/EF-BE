package com.nokcha.efbe.domain.profile.repository;

import com.nokcha.efbe.domain.profile.entity.UserCustomKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserCustomKeywordRepository extends JpaRepository<UserCustomKeyword, Long> {

    // 어드민 유저 상세 — 유저의 나만의 태그
    List<UserCustomKeyword> findByUserId(Long userId);

    // 매칭 배치 — 후보 풀 전체 한 번에 적재
    List<UserCustomKeyword> findByUserIdIn(Collection<Long> userIds);

    // 섹션 수정 — 유저의 커스텀 키워드 전체 삭제 후 재삽입 (전체 교체 패턴)
    @Modifying
    @Query("delete from UserCustomKeyword uck where uck.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
