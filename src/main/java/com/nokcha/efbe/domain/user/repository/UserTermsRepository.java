package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermsRepository extends JpaRepository<UserTerms, Long> {
}
