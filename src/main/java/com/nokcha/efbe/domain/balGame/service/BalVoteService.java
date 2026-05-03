package com.nokcha.efbe.domain.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.balGame.dto.response.BalVoteRspDto;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.entity.BalVote;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import com.nokcha.efbe.domain.balGame.repository.BalGameRepository;
import com.nokcha.efbe.domain.balGame.repository.BalVoteRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 투표 서비스 (중복 방지 + 수정 시 카운트 원자적 갱신)
@Service
@RequiredArgsConstructor
public class BalVoteService {

    private final BalGameRepository balGameRepository;
    private final BalVoteRepository balVoteRepository;
    private final UserRepository userRepository;

    // 신규 투표 처리 - 게임 행 락 후 카운트 + 투표 row 동시 처리
    @Transactional
    public BalVoteRspDto createVote(Long gameId, Long userId, BalVoteChoice choice) {
        BalGame game = balGameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_GAME));
        ensurePublished(game);

        if (balVoteRepository.existsByGameIdAndUserId(gameId, userId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_VOTE);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        try {
            balVoteRepository.save(BalVote.builder().game(game).user(user).choice(choice).build());
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_VOTE, e);
        }

        int aDelta = choice == BalVoteChoice.A ? 1 : 0;
        int bDelta = choice == BalVoteChoice.B ? 1 : 0;
        balGameRepository.updateVoteCounts(gameId, aDelta, bDelta);

        // total_count 는 DB Generated Column 이라 in-memory 엔티티에서 별도 계산
        int a = nz(game.getACount()) + aDelta;
        int b = nz(game.getBCount()) + bDelta;
        return BalVoteRspDto.of(gameId, choice, a + b, a, b);
    }

    // 투표 변경 처리 - 기존 차감 + 신규 가산을 단일 트랜잭션에서 원자 처리
    @Transactional
    public BalVoteRspDto updateVote(Long gameId, Long userId, BalVoteChoice newChoice) {
        BalGame game = balGameRepository.findByIdForUpdate(gameId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_GAME));
        ensurePublished(game);

        BalVote existing = balVoteRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_VOTE));

        if (existing.getChoice() == newChoice) {
            throw new BusinessException(ErrorCode.SAME_CHOICE_VOTE);
        }

        // 기존 -1, 신규 +1 (총합 변동 없음)
        int aDelta = (newChoice == BalVoteChoice.A ? 1 : 0) - (existing.getChoice() == BalVoteChoice.A ? 1 : 0);
        int bDelta = (newChoice == BalVoteChoice.B ? 1 : 0) - (existing.getChoice() == BalVoteChoice.B ? 1 : 0);

        // 단일 필드(choice) 변경만으로는 @LastModifiedDate 콜백 타이밍이 dirty-flush 에 의존하므로,
        // saveAndFlush 로 명시적 flush 를 트리거해 @PreUpdate → update_time 갱신을 보장한다.
        existing.changeChoice(newChoice);
        balVoteRepository.saveAndFlush(existing);

        balGameRepository.updateVoteCounts(gameId, aDelta, bDelta);

        int a = nz(game.getACount()) + aDelta;
        int b = nz(game.getBCount()) + bDelta;
        return BalVoteRspDto.of(gameId, newChoice, a + b, a, b);
    }

    // 내 투표 조회
    @Transactional(readOnly = true)
    public BalVoteRspDto getMyVote(Long gameId, Long userId) {
        BalGame game = balGameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_GAME));
        BalVote vote = balVoteRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_VOTE));
        int a = nz(game.getACount());
        int b = nz(game.getBCount());
        return BalVoteRspDto.of(gameId, vote.getChoice(), a + b, a, b);
    }

    private void ensurePublished(BalGame game) {
        if (game.getStatus() != BalGameStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.GAME_NOT_PUBLISHED);
        }
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
