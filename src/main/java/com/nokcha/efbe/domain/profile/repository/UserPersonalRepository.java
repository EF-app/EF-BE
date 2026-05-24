package com.nokcha.efbe.domain.profile.repository;

import com.nokcha.efbe.domain.profile.entity.UserPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPersonalRepository extends JpaRepository<UserPersonal, Long> {

    // 어드민 유저 상세 — 유저의 성향(SELF) / 이상형(IDEAL)
    List<UserPersonal> findByUserId(Long userId);
}