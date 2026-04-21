package com.nokcha.efbe.domain.area.repository;

import com.nokcha.efbe.domain.area.entity.CodeArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AreaRepository extends JpaRepository<CodeArea, Long> {
    Optional<CodeArea> findByCountryAndCity(String country, String city);
}