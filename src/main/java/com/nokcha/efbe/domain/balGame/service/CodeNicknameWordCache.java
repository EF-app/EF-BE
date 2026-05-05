package com.nokcha.efbe.domain.balGame.service;

import com.nokcha.efbe.domain.balGame.entity.CodeNicknameWord;
import com.nokcha.efbe.domain.balGame.entity.CodeNicknameWordType;
import com.nokcha.efbe.domain.balGame.repository.CodeNicknameWordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

// 닉네임 단어 메모리 캐시 — 기동 시 1회 로드 후 in-memory 랜덤 선택
// (ORDER BY RAND() 풀스캔 제거, 하루 수천~수만 댓글에도 추가 DB 부하 0)
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeNicknameWordCache {

    private static final Set<CodeNicknameWordType> NOUN_TYPES =
            Set.of(CodeNicknameWordType.ANIMAL, CodeNicknameWordType.FOOD, CodeNicknameWordType.NATURE);

    private final CodeNicknameWordRepository codeNicknameWordRepository;

    // 불변 스냅샷 (갱신 시 통째로 교체 — 읽기는 락 없이 안전)
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(Snapshot.empty());

    // 기동 시 1회 로드. 사전이 비어 있으면 서비스가 사용 시점에 예외 발생
    @PostConstruct
    public void load() {
        reload();
    }

    // 관리자가 사전 갱신 후 수동 호출할 수 있는 리로더
    public void reload() {
        List<CodeNicknameWord> all = codeNicknameWordRepository.findAll();
        List<String> adj = all.stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsActive()) && w.getType() == CodeNicknameWordType.ADJ)
                .map(CodeNicknameWord::getWord)
                .toList();
        List<String> noun = all.stream()
                .filter(w -> Boolean.TRUE.equals(w.getIsActive()) && NOUN_TYPES.contains(w.getType()))
                .map(CodeNicknameWord::getWord)
                .toList();
        snapshot.set(new Snapshot(adj, noun));
        log.info("[CodeNicknameWordCache] loaded ADJ={}, NOUN={}", adj.size(), noun.size());
    }

    // 랜덤 형용사 1개 (캐시 기반, O(1))
    public String randomAdjective() {
        return pickRandom(snapshot.get().adjectives);
    }

    // 랜덤 명사 1개 (캐시 기반, O(1))
    public String randomNoun() {
        return pickRandom(snapshot.get().nouns);
    }

    // 캐시 적재 여부 (서비스 방어적 체크용)
    public boolean isReady() {
        Snapshot s = snapshot.get();
        return !s.adjectives.isEmpty() && !s.nouns.isEmpty();
    }

    // 랜덤 인덱스 추출
    private String pickRandom(List<String> list) {
        if (list.isEmpty()) return null;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    // 불변 스냅샷 레코드 (List 는 이미 toList() 결과 = 불변)
    private record Snapshot(List<String> adjectives, List<String> nouns) {
        static Snapshot empty() {
            return new Snapshot(List.of(), List.of());
        }
    }
}
