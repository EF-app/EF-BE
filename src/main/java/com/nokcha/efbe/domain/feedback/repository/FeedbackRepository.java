package com.nokcha.efbe.domain.feedback.repository;

import com.nokcha.efbe.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
