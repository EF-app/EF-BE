package com.nokcha.efbe.domain.policy.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.policy.dto.response.PolicyDocumentDetailRspDto;
import com.nokcha.efbe.domain.policy.dto.response.PolicyDocumentSummaryRspDto;
import com.nokcha.efbe.domain.policy.entity.CodePolicyDocument;
import com.nokcha.efbe.domain.policy.repository.CodePolicyDocumentRepository;
import com.nokcha.efbe.domain.user.entity.TermType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PolicyDocumentService {

    private final CodePolicyDocumentRepository codePolicyDocumentRepository;

    // 활성 약관 목록 (각 policy_type 당 가장 최신 effective_date 1건만 노출)
    @Transactional(readOnly = true)
    public List<PolicyDocumentSummaryRspDto> getActivePolicies() {
        List<CodePolicyDocument> all = codePolicyDocumentRepository
                .findAllByIsActiveTrueOrderByPolicyTypeAscEffectiveDateDesc();

        Set<TermType> seen = EnumSet.noneOf(TermType.class);
        List<PolicyDocumentSummaryRspDto> result = new ArrayList<>();
        for (CodePolicyDocument doc : all) {
            if (seen.add(doc.getPolicyType())) {
                result.add(PolicyDocumentSummaryRspDto.from(doc));
            }
        }
        return result;
    }

    // 단건 상세 (특정 policy_type 의 최신 활성 버전 본문 포함)
    @Transactional(readOnly = true)
    public PolicyDocumentDetailRspDto getPolicyDetail(TermType policyType) {
        CodePolicyDocument doc = codePolicyDocumentRepository
                .findFirstByPolicyTypeAndIsActiveTrueOrderByEffectiveDateDesc(policyType)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POLICY_DOCUMENT));
        return PolicyDocumentDetailRspDto.from(doc);
    }
}
