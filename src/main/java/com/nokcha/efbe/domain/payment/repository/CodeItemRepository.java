package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.CodeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeItemRepository extends JpaRepository<CodeItem, String> {

    List<CodeItem> findByIsActiveTrueOrderBySortOrderAsc();
}
