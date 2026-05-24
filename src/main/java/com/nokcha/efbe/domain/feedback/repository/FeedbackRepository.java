package com.nokcha.efbe.domain.feedback.repository;

import com.nokcha.efbe.domain.feedback.entity.Feedback;
import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackStatus;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // 어드민 피드백 목록 — feedbackType / status / categoryCode / keyword(제목·내용 LIKE) 동적 필터
    @Query("select f from Feedback f " +
            "where (:feedbackType is null or f.feedbackType = :feedbackType) " +
            "and (:status is null or f.status = :status) " +
            "and (:categoryCode is null or f.categoryCode = :categoryCode) " +
            "and (:keyword is null " +
            "     or f.title like concat('%', :keyword, '%') " +
            "     or f.content like concat('%', :keyword, '%'))")
    Page<Feedback> searchForAdmin(@Param("feedbackType") FeedbackType feedbackType,
                                  @Param("status") FeedbackStatus status,
                                  @Param("categoryCode") FeedbackCategoryCode categoryCode,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);
}
