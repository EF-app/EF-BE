package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 로그인 아이디로 사용자 조회
    Optional<User> findByLoginId(String loginId);

    // 로그인 아이디 존재 여부 조회
    boolean existsByLoginId(String loginId);

    // 휴대폰 번호 존재 여부 조회
    boolean existsByPhone(String phone);

    // 닉네임 존재 여부 조회
    boolean existsByNickname(String nickname);

    // 동일 기기에서 계정이 바뀐 경우 기존 FCM 토큰 소유자 해제용
    Optional<User> findByFcmToken(String fcmToken);

    // 어드민 유저 목록 — keyword(닉네임/로그인ID/UUID LIKE) + status 다중 필터.
    @Query("select u from User u " +
            "where (:keyword is null " +
            "       or u.nickname like concat('%', :keyword, '%') " +
            "       or u.loginId like concat('%', :keyword, '%') " +
            "       or u.uuid like concat('%', :keyword, '%')) " +
            "and u.status in :statuses")
    Page<User> searchForAdmin(@Param("keyword") String keyword,
                              @Param("statuses") List<UserStatus> statuses,
                              Pageable pageable);

    // 무결성 검증 대상 — 탈퇴중 아닌 모든 유저
    @Query("select u from User u where u.status <> com.nokcha.efbe.domain.user.entity.UserStatus.WITHDRAWING " +
            "and u.status <> com.nokcha.efbe.domain.user.entity.UserStatus.WITHDRAWN")
    List<User> findAllNonWithdrawn();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update User u set u.lastActiveAt = :lastActiveAt where u.id = :userId")
    void updateLastActiveAt(@Param("userId") Long userId, @Param("lastActiveAt") LocalDateTime lastActiveAt);
}
