package com.nokcha.efbe.domain.profile.repository;

import com.nokcha.efbe.domain.profile.entity.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {

    // 어드민 유저 상세 — 유저의 관심사 키워드
    List<UserKeyword> findByUserId(Long userId);
}
