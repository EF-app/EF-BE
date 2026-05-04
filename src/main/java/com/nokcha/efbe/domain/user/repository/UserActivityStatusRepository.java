package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.UserActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityStatusRepository extends JpaRepository<UserActivityStatus, Long> {
}
