package com.nokcha.efbe.domain.profile.repository;

import com.nokcha.efbe.domain.profile.entity.UserKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserKeywordRepository extends JpaRepository<UserKeyword, Long> {
}
