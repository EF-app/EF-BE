package com.efbe.domain.profile.repository;

import com.efbe.domain.profile.entity.UserPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPersonalRepository extends JpaRepository<UserPersonal, Long> {
}