package com.nokcha.efbe.domain.feedback.repository;

import com.nokcha.efbe.domain.feedback.entity.FeedbackImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackImageRepository extends JpaRepository<FeedbackImage, Long> {

    List<FeedbackImage> findByFeedbackIdOrderBySortOrderAsc(Long feedbackId);
}
