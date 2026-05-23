package com.nokcha.efbe.domain.admin.account.repository;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

// 관리자 계정 "관리"용 별도 레포지토리. 시스템>관리자계정 화면 전용 검색·페이지 쿼리를 둔다.
@Repository
public interface AdminAccountManagementRepository extends JpaRepository<AdminAccount, Long> {

    // login_id 중복 체크 (생성 시)
    boolean existsByLoginId(String loginId);

    // 동적 검색 — keyword(LIKE: name / loginId / email), is_active 일치
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
