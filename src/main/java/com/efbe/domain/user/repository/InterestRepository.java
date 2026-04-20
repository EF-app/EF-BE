package com.efbe.domain.user.repository;

import com.efbe.domain.profile.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {

    // 대분류와 소분류로 관심사 조회
    Optional<Interest> findByBigCategoryAndSmallCategory(String bigCategory, String smallCategory);
}
