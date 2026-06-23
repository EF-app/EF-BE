package com.nokcha.efbe.domain.profile.event;

/**
 * 프로필 수정 commit 후 발행되는 도메인 이벤트.
 *  구독자:
 *    - 매칭 {@code MyFeedRecomputer} — kind.triggersRecompute() 일 때 본인 피드 즉시 재계산
 *
 *  발행 위치: {@code ProfileEditService} 의 큰 영향 수정 메서드 (updateBasic 의 areaId 변경 /
 *             updateIdeal 의 idealPointTypes 변경).
 */
public record ProfileUpdatedEvent(long userId, ProfileChangeKind kind) {}
