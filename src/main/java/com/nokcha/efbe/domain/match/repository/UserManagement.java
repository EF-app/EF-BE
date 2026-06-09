package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.UserContext;

import java.util.List;
import java.util.Map;

/**
 * 통합 지점 1 — 우리 User/UserProfile/Keyword/Personal/CodeArea → UserContext 매핑.
 *  여기가 도메인 ↔ 매칭 계산 영역의 유일한 경계.
 *  계산기/풀/슬롯/태그/배치는 UserContext 만 본다.
 *
 *  구현체: {@code UserManagementImpl} (Stage 4b).
 */
public interface UserManagement {

    /**
     * 후보 자격 풀 (하드 필터):
     *   user.status='ACTIVE' AND profile.status='APPROVED'
     *   AND last_active_at >= NOW() - cfg.lastActiveDays
     *   AND ABS(age - me.age) <= cfg.ageMaxDiff
     *   AND isDomestic(other) == isDomestic(me)        -- 국내/해외 그룹 일치
     *   AND id != me.id
     *   AND NOT block(me,other) AND NOT block(other,me)
     *   AND id NOT IN (SELECT target FROM match_actions
     *                  WHERE actor=me AND
     *                        (action IN ('LIKE','SUPER_LIKE','POWER_MESSAGE')
     *                         OR (action='PASS' AND expires_at >= NOW())))
     *
     *  반경 필터는 여기 SQL 의 바운딩박스 (좌표 인덱스 활용) → 정밀 haversine 은 CandidateSelector 가.
     */
    List<UserContext> findEligible(UserContext me, MatchingConfig cfg);

    /** 오늘 피드를 만들 대상 — 활성·승인 + 최근 접속. */
    List<UserContext> findEligibleViewers(MatchingConfig cfg);

    /** 단건 컨텍스트 — "받은 좋아요" 단건 계산용. */
    UserContext loadContext(long userId);

    /**
     * 다건 컨텍스트 — id 리스트를 batch fetch 로 한 번에 로드.
     */
    List<UserContext> loadContexts(List<Long> userIds);

    /** 콜드스타트: 전날 좋아요 많이 받은 사람 (지역·나이 필터 적용). */
    List<UserContext> topLikedYesterday(UserContext me, MatchingConfig cfg);

    /** 콜드스타트: 최근 접속자. */
    List<UserContext> recentlyActive(UserContext me, MatchingConfig cfg);

    /**
     * 신규자 fan-out 용 — 주어진 target 과 호환되는 viewer 의 id 목록 (전체).
     *  findEligible 의 호환 조건 (status/profile_status/last_active/age/그룹/block/match_actions) 을
     *
     *  RecentNewbieBatch — newcomer 가 등장할 viewer 추출.
     */
    List<Long> findCompatibleViewerIds(long targetUserId, MatchingConfig cfg);

    /**
     * 04:00 정상 배치 실패자 보정용 — 활성 viewer 인데 오늘 daily_feed row 가 없는 자들.
     *  findEligibleViewers 조건 + WHERE u.id NOT IN (SELECT viewer_id FROM match_daily_feed WHERE feed_date = CURDATE()).
     *
     *  NightlyMatchBatch.recoverFailedViewers — 05:00 KST 보정 배치.
     */
    List<UserContext> findFailedViewersToday(MatchingConfig cfg);

    /**
     * 활성 viewer 전체 ID → UserContext 매핑.
     *  배치 시작 시 1회만 호출. viewer 1명당 findEligible 의 loadContexts(7875 ids) 가 N² 부하인 걸
     *  Map lookup (O(1)) 으로 줄임. 5만 명 환경에서 12h → 30분 미만으로.
     *
     *  메모리: 5만 UserContext × ~1KB ≈ 50MB. 배치 끝나면 자동 GC.
     */
    Map<Long, UserContext> loadAllActiveContextsAsMap(MatchingConfig cfg);

    /**
     *  findEligible 의 하드필터를 SQL 안에서만 적용 후 ID 만 반환.
     *
     *  bbox 좌표 필터로 후보를 풀스캔 → 인덱스 range 로 좁힘. radiusKm 100km bbox.
     *      해외 그룹은 좌표 필터 적용 안 함 (해외 그룹은 작아서 풀스캔 OK).
     *
     *  → 무작위 추출은 호출처(CandidateSelector) 의 메모리 셔플이 담당). bbox 안 후보가
     *  수만 명을 넘어가면 random offset / id-range 패턴으로 전환 검토.
     */
    List<Long> findEligibleIds(UserContext me, MatchingConfig cfg, int radiusKm);
}
