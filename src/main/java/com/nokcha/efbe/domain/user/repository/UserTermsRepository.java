package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.UserPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermsRepository extends JpaRepository<UserPolicy, Long> {
}
