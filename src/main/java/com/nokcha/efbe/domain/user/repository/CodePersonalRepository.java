package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.profile.entity.CodePersonal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CodePersonalRepository extends JpaRepository<CodePersonal, Long> {

    // 대분류와 소분류로 성향 정보 조회
    Optional<CodePersonal> findByBigCategoryAndSmallCategory(String bigCategory, String smallCategory);

    // 카테고리 그룹으로 personalId 조회(나누어 수정하기 때문)
    List<CodePersonal> findByBigCategoryIn(Collection<String> bigCategories);
}
