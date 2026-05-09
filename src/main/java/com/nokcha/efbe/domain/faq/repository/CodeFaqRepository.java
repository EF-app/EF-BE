package com.nokcha.efbe.domain.faq.repository;

import com.nokcha.efbe.domain.faq.entity.CodeFaq;
import com.nokcha.efbe.domain.faq.entity.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeFaqRepository extends JpaRepository<CodeFaq, Long> {

    // 활성 FAQ 전체, display_order ASC, id ASC 보조 정렬 (동일 order 안정성)
    List<CodeFaq> findAllByIsActiveTrueOrderByDisplayOrderAscIdAsc();

    // 카테고리 필터 + 활성 + display_order ASC
    List<CodeFaq> findAllByIsActiveTrueAndCategoryOrderByDisplayOrderAscIdAsc(FaqCategory category);

    // 인기 FAQ 만 (활성 + is_popular)
    List<CodeFaq> findAllByIsActiveTrueAndIsPopularTrueOrderByDisplayOrderAscIdAsc();
}
