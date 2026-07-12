package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.RcProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RcProcessedEventRepository extends JpaRepository<RcProcessedEvent, String> {

    boolean existsByEventId(String eventId);
}
