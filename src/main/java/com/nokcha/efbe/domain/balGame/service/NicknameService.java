package com.nokcha.efbe.domain.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalNameMap;
import com.nokcha.efbe.domain.balGame.repository.BalNameMapRepository;
import com.nokcha.efbe.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

// 익명 닉네임 생성/조회 서비스
// (v2.0 사양: code_nickname_word 사전 사용 → 최대 3회 재추첨 → '#숫자4자리' 폴백)
// 사전 조회는 CodeNicknameWordCache 를 거쳐 메모리에서 처리 (ORDER BY RAND() 제거)
@Service
@RequiredArgsConstructor
public class NicknameService {

    private static final int MAX_RETRY = 3;
    private static final int SUFFIX_BOUND = 10_000;

    private final BalNameMapRepository balNameMapRepository;
    private final CodeNicknameWordCache codeNicknameWordCache;

    // 해당 게임의 유저 닉네임을 조회, 없으면 새로 생성
    @Transactional
    public String resolveOrCreate(BalGame game, User user) {
        return balNameMapRepository.findByGameIdAndUserId(game.getId(), user.getId())
                .map(BalNameMap::getNickname)
                .orElseGet(() -> generateAndSave(game, user));
    }

    // v2.0 알고리즘: 3회까지 재추첨, 실패 시 '#숫자4자리' 접미사로 유니크 강제
    private String generateAndSave(BalGame game, User user) {
        if (!codeNicknameWordCache.isReady()) {
            throw new BusinessException(ErrorCode.NICKNAME_GENERATION_FAILED);
        }
        Set<String> used = new HashSet<>(balNameMapRepository.findUsedNicknames(game.getId()));

        String picked = null;
        for (int attempt = 0; attempt < MAX_RETRY; attempt++) {
            String candidate = pickCandidate();
            if (!used.contains(candidate)) {
                picked = candidate;
                break;
            }
        }
        if (picked == null) {
            picked = pickCandidate() + suffix();
        }

        try {
            balNameMapRepository.save(BalNameMap.builder().game(game).user(user).nickname(picked).build());
            return picked;
        } catch (DataIntegrityViolationException e) {
            // 동시성 충돌 시 접미사 붙여 1회 재시도
            String retry = pickCandidate() + suffix();
            try {
                balNameMapRepository.save(BalNameMap.builder().game(game).user(user).nickname(retry).build());
                return retry;
            } catch (DataIntegrityViolationException inner) {
                throw new BusinessException(ErrorCode.NICKNAME_GENERATION_FAILED, inner);
            }
        }
    }

    // 랜덤 '형용사 + 명사' 후보 (메모리 캐시 기반)
    private String pickCandidate() {
        String adj = codeNicknameWordCache.randomAdjective();
        String noun = codeNicknameWordCache.randomNoun();
        if (adj == null || noun == null) {
            throw new BusinessException(ErrorCode.NICKNAME_GENERATION_FAILED);
        }
        return adj + " " + noun;
    }

    // '#숫자4자리' 접미사 (0000~9999)
    private String suffix() {
        return "#" + String.format("%04d", ThreadLocalRandom.current().nextInt(SUFFIX_BOUND));
    }
}
