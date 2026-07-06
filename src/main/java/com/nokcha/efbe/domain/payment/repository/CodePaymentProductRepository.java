package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.CodePaymentProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodePaymentProductRepository extends JpaRepository<CodePaymentProduct, Long> {

    Optional<CodePaymentProduct> findByProductCode(String productCode);

    List<CodePaymentProduct> findByIsActiveTrueOrderBySortOrderAsc();
}
