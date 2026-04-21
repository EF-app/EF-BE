package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.profile.entity.Personal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalRepository extends JpaRepository<Personal, Long> {

    // 대분류와 소분류로 성향 정보 조회
    Optional<Personal> findByBigCategoryAndSmallCategory(String bigCategory, String smallCategory);
}
