package com.nokcha.efbe.domain.admin.block.service;

import com.nokcha.efbe.domain.admin.block.dto.response.AdminBlockRspDto;
import com.nokcha.efbe.domain.block.entity.Block;
import com.nokcha.efbe.domain.block.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBlockService {

    private final BlockRepository blockRepository;

    @Transactional(readOnly = true)
    public Page<AdminBlockRspDto> getBlocks(String keyword, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Block> page = blockRepository.searchForAdmin(kw, pageable);

        // 상호 차단(mutual) 판정 — 페이지 유저들 사이의 모든 차단 쌍을 한 번에 조회.
        Set<Long> userIds = new HashSet<>();
        for (Block b : page.getContent()) {
            userIds.add(b.getBlocker().getId());
            userIds.add(b.getBlocked().getId());
        }
        Set<String> pairSet = userIds.isEmpty()
                ? Set.of()
                : blockRepository.findByBlocker_IdInAndBlocked_IdIn(userIds, userIds).stream()
                .map(b -> pairKey(b.getBlocker().getId(), b.getBlocked().getId()))
                .collect(Collectors.toSet());

        return page.map(b -> {
            boolean mutual = pairSet.contains(pairKey(b.getBlocked().getId(), b.getBlocker().getId()));
            return AdminBlockRspDto.from(b, mutual);
        });
    }

    private static String pairKey(Long blockerId, Long blockedId) {
        return blockerId + "-" + blockedId;
    }
}
