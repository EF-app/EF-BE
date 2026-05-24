package com.nokcha.efbe.domain.admin.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalVoteBucketStat;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalVoteRspDto;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalVoteStatsRspDto;
import com.nokcha.efbe.domain.admin.balGame.repository.AdminBalVoteQueryRepository;
import com.nokcha.efbe.domain.admin.balGame.repository.projection.AdminBalVoteBucketRow;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import com.nokcha.efbe.domain.balGame.repository.BalGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 어드민 측 밸런스 게임 투표 서비스 (목록 + 통계)
@Service
@RequiredArgsConstructor
public class AdminBalVoteService {

    private final AdminBalVoteQueryRepository adminBalVoteQueryRepository;
    private final BalGameRepository balGameRepository;

    // 개별 투표자 목록 — id 기반
    @Transactional(readOnly = true)
    public Page<AdminBalVoteRspDto> getVotes(Long gameId, BalVoteChoice choice, Pageable pageable) {
        if (!balGameRepository.existsById(gameId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_GAME);
        }
        return adminBalVoteQueryRepository.findAdminVotes(gameId, choice, pageable)
                .map(AdminBalVoteRspDto::from);
    }

    // 통계 — aPercent/bPercent + 연령대/지역 분포.
    @Transactional(readOnly = true)
    public AdminBalVoteStatsRspDto getStats(Long gameId) {
        BalGame game = balGameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_GAME));

        int a = game.getACount() == null ? 0 : game.getACount();
        int b = game.getBCount() == null ? 0 : game.getBCount();
        int total = a + b;
        Double aPercent = total == 0 ? null : Math.round(((double) a / total) * 1000) / 10.0;
        Double bPercent = total == 0 ? null : Math.round(((double) b / total) * 1000) / 10.0;

        return AdminBalVoteStatsRspDto.builder()
                .aPercent(aPercent)
                .bPercent(bPercent)
                .ageDistribution(toBucketMap(adminBalVoteQueryRepository.aggregateVotesByAge(gameId)))
                .areaDistribution(toBucketMap(adminBalVoteQueryRepository.aggregateVotesByArea(gameId)))
                .build();
    }

    private Map<String, AdminBalVoteBucketStat> toBucketMap(List<AdminBalVoteBucketRow> rows) {
        Map<String, int[]> agg = new HashMap<>();
        for (AdminBalVoteBucketRow r : rows) {
            int[] ab = agg.computeIfAbsent(r.bucketLabel(), k -> new int[2]);
            if (r.choice() == BalVoteChoice.A) ab[0] += (int) r.count();
            else if (r.choice() == BalVoteChoice.B) ab[1] += (int) r.count();
        }
        Map<String, AdminBalVoteBucketStat> result = new HashMap<>();
        agg.forEach((label, ab) -> result.put(label, new AdminBalVoteBucketStat(ab[0], ab[1])));
        return result;
    }
}
