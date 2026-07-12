package com.nokcha.efbe.domain.profile.repository;

import com.nokcha.efbe.domain.profile.entity.UserPersonal;
import com.nokcha.efbe.domain.profile.entity.UserPersonalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserPersonalRepository extends JpaRepository<UserPersonal, Long> {

    // 어드민 유저 상세 — 유저의 성향(SELF) / 이상형(IDEAL)
    List<UserPersonal> findByUserId(Long userId);

    // 매칭 배치 — 후보 풀 전체 한 번에 적재
    List<UserPersonal> findByUserIdIn(Collection<Long> userIds);

    // 섹션 수정 — 특정 type 의 row 만 일괄 삭제 후 재삽입(1단계 ideal / self)
    @Modifying
    @Query("delete from UserPersonal up where up.userId = :userId and up.type = :type")
    void deleteByUserIdAndType(@Param("userId") Long userId, @Param("type") UserPersonalType type);

    // 탈퇴 파기용 (SELF/IDEAL 전체)
    @Modifying
    @Query("delete from UserPersonal up where up.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // 카테고리만 분리 삭제(self : lifestyle / about-me / my-style 각각)
    @Modifying
    @Query("delete from UserPersonal up " +
            "where up.userId = :userId and up.type = :type and up.personalId in :personalIds")
    void deleteByUserIdAndTypeAndPersonalIdIn(@Param("userId") Long userId,
                                              @Param("type") UserPersonalType type,
                                              @Param("personalIds") Collection<Long> personalIds);
}