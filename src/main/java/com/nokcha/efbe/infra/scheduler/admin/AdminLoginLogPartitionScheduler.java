package com.nokcha.efbe.infra.scheduler.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

// admin_login_log 월별 파티션 유지보수.
//   1) 2개월 뒤 boundary 의 파티션을 미리 추가 (pmax 를 reorganize)
//   2) RETENTION_MONTHS 보다 오래된 파티션은 DROP — 즉시 비움, 락 부담 거의 없음
// DB 가 파티션 미적용 상태이거나 pmax 미존재면 ALTER 가 실패 → ERROR 로그만 남기고 다음 실행 대기.
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminLoginLogPartitionScheduler {

    // 보존 개월 수 (실패 로그 분석을 위해 최소 6개월 권장).
    private static final int RETENTION_MONTHS = 6;

    private static final DateTimeFormatter PART_NAME_FMT = DateTimeFormatter.ofPattern("'p'yyyyMM");
    private static final DateTimeFormatter BOUNDARY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JdbcTemplate jdbcTemplate;

    // 매일 03:00 — 트래픽이 가장 적은 시간대.
    // @Scheduled(cron = "0 0 3 * * *")
    // @SchedulerLock(name = "AdminLoginLogPartitionScheduler", lockAtMostFor = "PT10M", lockAtLeastFor = "PT30S")
    public void maintain() {
        addUpcomingPartition();
        dropExpiredPartitions();
    }

    // 2개월 뒤 boundary 를 위한 파티션을 pmax 에서 잘라낸다.
    private void addUpcomingPartition() {
        YearMonth target = YearMonth.now().plusMonths(2);
        String partName = target.atDay(1).format(PART_NAME_FMT);
        String boundary = target.atDay(1).format(BOUNDARY_FMT);

        try {
            jdbcTemplate.execute(String.format(
                    "ALTER TABLE admin_login_log REORGANIZE PARTITION pmax INTO (" +
                            "PARTITION %s VALUES LESS THAN ('%s'), " +
                            "PARTITION pmax VALUES LESS THAN (MAXVALUE))",
                    partName, boundary
            ));
            log.info("[AdminLoginLogPartitionScheduler] added partition {}", partName);
        } catch (DataAccessException e) {
            log.error("[AdminLoginLogPartitionScheduler] add partition {} failed: {}", partName, e.getMessage());
        }
    }

    // RETENTION_MONTHS 이전 파티션 DROP. pmax 는 절대 건드리지 않는다.
    private void dropExpiredPartitions() {
        String cutoff = YearMonth.now().minusMonths(RETENTION_MONTHS).atDay(1).format(PART_NAME_FMT);

        List<String> targets = jdbcTemplate.queryForList(
                "SELECT PARTITION_NAME FROM INFORMATION_SCHEMA.PARTITIONS " +
                        "WHERE TABLE_SCHEMA = DATABASE() " +
                        "  AND TABLE_NAME = 'admin_login_log' " +
                        "  AND PARTITION_NAME IS NOT NULL " +
                        "  AND PARTITION_NAME != 'pmax' " +
                        "  AND PARTITION_NAME < ? " +
                        "ORDER BY PARTITION_NAME",
                String.class,
                cutoff
        );

        for (String part : targets) {
            try {
                jdbcTemplate.execute("ALTER TABLE admin_login_log DROP PARTITION " + part);
                log.info("[AdminLoginLogPartitionScheduler] dropped partition {}", part);
            } catch (DataAccessException e) {
                log.error("[AdminLoginLogPartitionScheduler] drop partition {} failed: {}", part, e.getMessage());
            }
        }
    }
}
