package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.profile.entity.CodeKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeywordRepository extends JpaRepository<CodeKeyword, Long> {

    // 대분류와 소분류로 키워드 조회
    Optional<CodeKeyword> findByBigCategoryAndSmallCategory(String bigCategory, String smallCategory);
}
