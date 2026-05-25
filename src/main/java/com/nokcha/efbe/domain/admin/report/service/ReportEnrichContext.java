package com.nokcha.efbe.domain.admin.report.service;

import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.user.entity.User;

import java.util.Map;

// AdminReportService 가 그룹화 응답 enrich 시 batch fetch 결과를 담는 in-memory 컨텍스트.
// target_type 별로 한 번씩만 fetch — N+1 회피.
// CHAT / CHAT_IMAGE 는 현 단계 미지원 — 추후 추가 시 필드 확장.
public record ReportEnrichContext(
        Map<Long, User> users,
        Map<Long, PostIt> postIts,
        Map<Long, BalGameComment> balComments
) {
    public static ReportEnrichContext empty() {
        return new ReportEnrichContext(Map.of(), Map.of(), Map.of());
    }
}
