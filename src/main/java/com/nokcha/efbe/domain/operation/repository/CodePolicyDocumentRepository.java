package com.nokcha.efbe.domain.operation.repository;

import com.nokcha.efbe.domain.operation.entity.CodePolicyDocument;
import com.nokcha.efbe.domain.user.entity.TermType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodePolicyDocumentRepository extends JpaRepository<CodePolicyDocument, Long> {

    Optional<CodePolicyDocument> findFirstByPolicyTypeAndIsActiveTrueOrderByEffectiveDateDesc(TermType policyType);

    // 활성 정책 전체 (같은 policy_type 에 다중 활성 버전이 들어와도 최신 effective_date 가 먼저 오도록 정렬)
    List<CodePolicyDocument> findAllByIsActiveTrueOrderByPolicyTypeAscEffectiveDateDesc();

    boolean existsByPolicyTypeAndVersion(TermType policyType, String version);
}
