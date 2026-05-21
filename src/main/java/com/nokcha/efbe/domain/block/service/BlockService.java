package com.nokcha.efbe.domain.block.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.block.dto.request.BlockCreateReqDto;
import com.nokcha.efbe.domain.block.dto.response.BlockRspDto;
import com.nokcha.efbe.domain.block.dto.response.BlockedUserRspDto;
import com.nokcha.efbe.domain.block.entity.Block;
import com.nokcha.efbe.domain.block.repository.BlockRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 유저 간 차단
@Slf4j
@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;

    // 차단 생성. 자기 자신 차단 불가, 이미 차단한 유저면 중복 거부.
    @Transactional
    public BlockRspDto createBlock(Long blockerId, BlockCreateReqDto req) {
        Long blockedId = req.getBlockedUserId();

        if (blockedId.equals(blockerId)) {
            throw new BusinessException(ErrorCode.SELF_ACTION_FORBIDDEN);
        }

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        if (blockRepository.existsByBlocker_IdAndBlocked_Id(blockerId, blockedId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_BLOCK);
        }

        try {
            Block block = blockRepository.save(Block.builder()
                    .blocker(blocker)
                    .blocked(blocked)
                    .reasonCategory(req.getReasonCategory())
                    .detail(req.getDetail())
                    .build());
            log.info("[Block] blocker={} blocked={} reason={}",
                    blockerId, blockedId, block.getReasonCategory());
            return BlockRspDto.from(block);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_BLOCK, e);
        }
    }

    // 차단 해제. blocker 가 blockedUserId 를 차단한 기록이 없으면 거부.
    @Transactional
    public void unblock(Long blockerId, Long blockedUserId) {
        Block block = blockRepository.findByBlocker_IdAndBlocked_Id(blockerId, blockedUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_BLOCK));
        blockRepository.delete(block);
        log.info("[Block] unblock blocker={} blocked={}", blockerId, blockedUserId);
    }

    // 내 차단 목록
    @Transactional(readOnly = true)
    public List<BlockedUserRspDto> getMyBlocks(Long blockerId) {
        return blockRepository.findByBlocker_IdOrderByCreateTimeDesc(blockerId).stream()
                .map(b -> {
                    User blocked = b.getBlocked();
                    String area = blocked.getAreaId() == null ? null
                            : areaRepository.findById(blocked.getAreaId())
                            .map(BlockService::composeLocation)
                            .orElse(null);
                    return BlockedUserRspDto.of(b, area);
                })
                .toList();
    }

    // CodeArea(country, city)
    private static String composeLocation(CodeArea area) {
        String country = area.getCountry();
        String city = area.getCity();
        boolean hasCountry = country != null && !country.isBlank();
        boolean hasCity = city != null && !city.isBlank();
        if (!hasCountry && !hasCity) return null;
        if (hasCountry && hasCity) return country + " " + city;
        return hasCountry ? country : city;
    }
}
