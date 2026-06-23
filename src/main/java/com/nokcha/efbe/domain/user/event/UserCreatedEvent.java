package com.nokcha.efbe.domain.user.event;

/**
 * 회원가입 완료 트랜잭션 commit 후 발행되는 도메인 이벤트.
 *  구독자:
 *    - 매칭 ColdStartFeed — 가입 당일 임시 피드 생성
 *
 *  ※ 발행 위치: {@code UserAuthService.completeSignUp(...)} 안.
 *     트랜잭션 안에서 publishEvent 하고, @TransactionalEventListener(AFTER_COMMIT) 로 받는다.
 */
public record UserCreatedEvent(long userId) {}
