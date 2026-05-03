package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.profile.entity.CodeInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestRepository extends JpaRepository<CodeInterest, Long> {

    // 대분류와 소분류로 관심사 조회
    Optional<CodeInterest> findByBigCategoryAndSmallCategory(String bigCategory, String smallCategory);
}
