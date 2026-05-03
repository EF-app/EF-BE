package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.profile.entity.CodePersonal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalRepository extends JpaRepository<CodePersonal, Long> {

    // 대분류와 소분류로 성향 정보 조회
    Optional<CodePersonal> findByBigCategoryAndSmallCategory(String bigCategory, String smallCategory);
}
