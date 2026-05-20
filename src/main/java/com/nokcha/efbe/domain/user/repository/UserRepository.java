package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.BanStatus;
import com.nokcha.efbe.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 어드민 유저 목록 — keyword(닉네임/로그인ID/UUID LIKE) + banStatus 다중 필터.
    @Query("select u from User u " +
            "where (:keyword is null " +
            "       or u.nickname like concat('%', :keyword, '%') " +
            "       or u.loginId like concat('%', :keyword, '%') " +
            "       or u.uuid like concat('%', :keyword, '%')) " +
            "and u.banStatus in :statuses")
    Page<User> searchForAdmin(@Param("keyword") String keyword,
                              @Param("statuses") List<BanStatus> statuses,
                              Pageable pageable);
}
