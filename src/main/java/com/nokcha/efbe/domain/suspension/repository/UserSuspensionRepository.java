package com.nokcha.efbe.domain.suspension.repository;

import com.nokcha.efbe.domain.suspension.entity.UserSuspension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 시스템(배치)/유저(본인 활성 조회)/도메인(평가)
public interface UserSuspensionRepository extends JpaRepository<UserSuspension, Long> {

    /**
     * 특정 유저의 활성 제재: is_lifted=false AND (ends_at IS NULL OR ends_at > now)
     */
    @Query("select s from UserSuspension s " +
            "where s.user.id = :userId " +
            "  and s.isLifted = false " +
            "  and (s.endsAt is null or s.endsAt > :now) " +
            "order by s.id desc")
    List<UserSuspension> findActiveByUserId(@Param("userId") Long userId,
                                            @Param("now") LocalDateTime now);

    /**
     * 특정 유저의 활성 제재 목록 — 강한 등급부터 정렬 (PERMANENT > TEMPORARY > WARNING).
     * WARNING 은 5건까지 누적 가능하므로 결과가 여러 건일 수 있음. 호출부에서 첫 element 사용.
     */
    @Query("select s from UserSuspension s " +
            "where s.user.id = :userId " +
            "  and s.isLifted = false " +
            "  and (s.endsAt is null or s.endsAt > :now) " +
            "order by case s.suspensionType " +
            "          when com.nokcha.efbe.domain.suspension.entity.SuspensionType.PERMANENT then 0 " +
            "          when com.nokcha.efbe.domain.suspension.entity.SuspensionType.TEMPORARY then 1 " +
            "          else 2 end, s.id desc")
    List<UserSuspension> findActiveSortedByStrongest(@Param("userId") Long userId,
                                                     @Param("now") LocalDateTime now,
                                                     Pageable pageable);

    default Optional<UserSuspension> findStrongestActiveByUserId(Long userId, LocalDateTime now) {
        return findActiveSortedByStrongest(userId, now, PageRequest.of(0, 1))
                .stream().findFirst();
    }

    /**
     * 자동만료 배치(매일 00:00) 용:
     * ends_at 이 :now 이전이고 is_lifted=false 인 활성 제재 row 목록 (WARNING/TEMPORARY).
     * PERMANENT 는 ends_at=null 이라 자연히 제외됨.
     */
    @Query("select s from UserSuspension s " +
            "where s.isLifted = false " +
            "  and s.endsAt is not null " +
            "  and s.endsAt <= :now")
    List<UserSuspension> findJustExpiredSuspensions(@Param("now") LocalDateTime now);
}
