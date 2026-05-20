package com.nokcha.efbe.domain.profile.repository;

import com.nokcha.efbe.domain.profile.entity.UserCustomKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCustomKeywordRepository extends JpaRepository<UserCustomKeyword, Long> {

    // 어드민 유저 상세 — 유저의 나만의 태그
    List<UserCustomKeyword> findByUserId(Long userId);
}
