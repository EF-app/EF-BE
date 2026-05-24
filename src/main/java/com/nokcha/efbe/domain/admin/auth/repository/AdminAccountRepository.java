package com.nokcha.efbe.domain.admin.auth.repository;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {

    // 로그인 아이디로 관리자 조회
    Optional<AdminAccount> findByLoginId(String loginId);

    // 로그인 아이디 존재 여부 조회
    boolean existsByLoginId(String loginId);

    // 시스템 > 관리자계정 목록 화면
    @Query("select a from AdminAccount a " +
            "where (:isActive is null or a.isActive = :isActive) " +
            "and (:keyword is null " +
            "     or a.name      like concat('%', :keyword, '%') " +
            "     or a.loginId   like concat('%', :keyword, '%') " +
            "     or a.email     like concat('%', :keyword, '%'))")
    Page<AdminAccount> search(@Param("keyword") String keyword,
                              @Param("isActive") Boolean isActive,
                              Pageable pageable);
}
