package com.nokcha.efbe.domain.faq.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.faq.dto.response.FaqRspDto;
import com.nokcha.efbe.domain.faq.entity.CodeFaq;
import com.nokcha.efbe.domain.faq.entity.FaqCategory;
import com.nokcha.efbe.domain.faq.repository.CodeFaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 도움말/FAQ 조회 서비스 (사용자 측 — 활성 데이터만)
@Service
@RequiredArgsConstructor
public class FaqService {

    private static final String CATEGORY_ALL = "all";

    private final CodeFaqRepository codeFaqRepository;

    // 활성 FAQ 목록. categoryKey 가 null/blank/"all" 이면 전체, 아니면 해당 카테고리만.
    @Transactional(readOnly = true)
    public List<FaqRspDto> getFaqs(String categoryKey) {
        List<CodeFaq> rows = (categoryKey == null || categoryKey.isBlank() || CATEGORY_ALL.equalsIgnoreCase(categoryKey))
                ? codeFaqRepository.findAllByIsActiveTrueOrderByDisplayOrderAscIdAsc()
                : findByCategoryKey(categoryKey);
        return rows.stream().map(FaqRspDto::from).toList();
    }

    // 인기 FAQ 만 (활성 + is_popular=true)
    @Transactional(readOnly = true)
    public List<FaqRspDto> getPopularFaqs() {
        return codeFaqRepository.findAllByIsActiveTrueAndIsPopularTrueOrderByDisplayOrderAscIdAsc()
                .stream().map(FaqRspDto::from).toList();
    }

    private List<CodeFaq> findByCategoryKey(String categoryKey) {
        FaqCategory category = FaqCategory.fromApiKey(categoryKey);
        if (category == null) throw new BusinessException(ErrorCode.NOT_FOUND_CATEGORY);
        return codeFaqRepository.findAllByIsActiveTrueAndCategoryOrderByDisplayOrderAscIdAsc(category);
    }
}
