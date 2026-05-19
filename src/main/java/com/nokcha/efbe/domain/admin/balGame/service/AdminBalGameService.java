package com.nokcha.efbe.domain.admin.balGame.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalCommentRspDto;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalGameDetailRspDto;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalGameSummaryRspDto;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalVoteStatsRspDto;
import com.nokcha.efbe.domain.admin.balGame.dto.request.AdminBalGameCreateReqDto;
import com.nokcha.efbe.domain.admin.balGame.dto.request.AdminBalGameUpdateReqDto;
import com.nokcha.efbe.domain.balGame.entity.BalApply;
import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import com.nokcha.efbe.domain.balGame.repository.BalApplyRepository;
import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.entity.BalVote;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import com.nokcha.efbe.domain.balGame.repository.BalGameCommentRepository;
import com.nokcha.efbe.domain.balGame.repository.BalGameRepository;
import com.nokcha.efbe.domain.balGame.repository.BalVoteRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

// User 측 BalGameService 와 차이:
//   - status 차단 없음 (DRAFT/SCHEDULED/HIDDEN/ARCHIVED 모두 노출)
//   - Spring Page<> 페이지네이션 (CursorPage 아님)
@Service
@RequiredArgsConstructor
public class AdminBalGameService {

    private final BalGameRepository balGameRepository;
    private final BalGameCommentRepository balGameCommentRepository;
    private final BalVoteRepository balVoteRepository;
    private final UserRepository userRepository;
    private final BalApplyRepository balApplyRepository;
    private final AdminBalVoteService adminBalVoteService;

    @Transactional(readOnly = true)
    public Page<AdminBalGameSummaryRspDto> getGames(BalGameStatus status, BalCategoryCode categoryCode, Pageable pageable) {
        return balGameRepository.findAdminGames(status, categoryCode, pageable)
                .map(AdminBalGameSummaryRspDto::from);
    }

    // 단건 상세 — gameUuid 기반. status 차단 없이 모든 상태 노출. voteStats(비율 + 연령대/지역 분포)
    @Transactional(readOnly = true)
    public AdminBalGameDetailRspDto getGame(String gameUuid) {
        BalGame game = balGameRepository.findByUuid(gameUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_GAME));
        AdminBalVoteStatsRspDto stats = adminBalVoteService.getStats(game.getId());
        return AdminBalGameDetailRspDto.from(game, stats);
    }

    @Transactional
    public AdminBalGameDetailRspDto createGame(AdminBalGameCreateReqDto req) {
        BalGameStatus status = req.getStatus() == null ? BalGameStatus.DRAFT : req.getStatus();

        // 1) 신규 등록에 ARCHIVED 금지
        if (status == BalGameStatus.ARCHIVED) {
            throw new BusinessException(ErrorCode.BAL_GAME_INVALID_TRANSITION);
        }

        // 2) SCHEDULED 검증 — scheduledAt 필수, 미래, 10분 단위
        if (status == BalGameStatus.SCHEDULED) {
            validateScheduledAtForCreate(req.getScheduledAt());
        }

        // 3) scheduledEndAt 검증 (있을 때만)
        if (req.getScheduledEndAt() != null) {
            validateScheduledEndAtForCreate(req.getScheduledEndAt(), req.getScheduledAt());
        }

        // 4) BAL-APPLY 기반 등록 — applyId 있으면 PENDING → APPROVED 처리 + 신청자 도출
        User applicant = null;
        if (req.getApplyId() != null) {
            BalApply apply = balApplyRepository.findById(req.getApplyId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_APPLY));
            if (apply.getStatus() != BalApplyStatus.PENDING) {
                throw new BusinessException(ErrorCode.INVALID_GAME_STATUS);
            }
            apply.decide(BalApplyStatus.APPROVED, null);
            applicant = apply.getUser();
        }

        BalGame saved = balGameRepository.save(BalGame.builder()
                .uuid(UUID.randomUUID().toString())
                .optionA(req.getOptionA())
                .optionB(req.getOptionB())
                .optionADesc(req.getOptionADesc())
                .optionBDesc(req.getOptionBDesc())
                .optionAEmoji(req.getOptionAEmoji())
                .optionBEmoji(req.getOptionBEmoji())
                .description(req.getDescription())
                .categoryCode(req.getCategoryCode())
                .status(status)
                .scheduledAt(req.getScheduledAt())
                .scheduledEndAt(req.getScheduledEndAt())
                .applicant(applicant)
                .build());

        return AdminBalGameDetailRspDto.from(saved);
    }

    // SCHEDULED 신규 등록
    private void validateScheduledAtForCreate(LocalDateTime scheduledAt) {
        if (scheduledAt == null || !scheduledAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
        }
        if (scheduledAt.getMinute() % 10 != 0
                || scheduledAt.getSecond() != 0
                || scheduledAt.getNano() != 0) {
            throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
        }
    }

    // scheduledEndAt 검증
    private void validateScheduledEndAtForCreate(LocalDateTime endAt, LocalDateTime scheduledAt) {
        if (!endAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
        }
        if (scheduledAt != null && !endAt.isAfter(scheduledAt)) {
            throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
        }
    }

    @Transactional
    public AdminBalGameDetailRspDto updateGame(String gameUuid, AdminBalGameUpdateReqDto req) {
        BalGame game = balGameRepository.findByUuid(gameUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_GAME));

        // 1) PUBLISHED / ARCHIVED 수정 거부 (status/일정만 허용)
        BalGameStatus current = game.getStatus();
        if ((current == BalGameStatus.PUBLISHED || current == BalGameStatus.ARCHIVED)
                && req.hasContentField()) {
            throw new BusinessException(ErrorCode.BAL_GAME_CONTENT_LOCKED);
        }

        // 2) clear flag 와 value 동시 사용 모순 거부
        if (Boolean.TRUE.equals(req.getClearScheduledAt()) && req.getScheduledAt() != null) {
            throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
        }
        if (Boolean.TRUE.equals(req.getClearScheduledEndAt()) && req.getScheduledEndAt() != null) {
            throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
        }

        // 3) 상태 전환 검증 + 적용
        if (req.getStatus() != null && req.getStatus() != game.getStatus()) {
            validateTransition(game.getStatus(), req.getStatus(), req);
            game.changeStatus(req.getStatus());
        }

        // 4) 일정 — clear flag 우선, 그 다음 value
        if (Boolean.TRUE.equals(req.getClearScheduledAt())) {
            game.changeScheduledAt(null);
        } else if (req.getScheduledAt() != null) {
            validateScheduledAt(req.getScheduledAt(), game.getStatus());
            game.changeScheduledAt(req.getScheduledAt());
        }
        if (Boolean.TRUE.equals(req.getClearScheduledEndAt())) {
            game.changeScheduledEndAt(null);
        } else if (req.getScheduledEndAt() != null) {
            validateScheduledEndAt(req.getScheduledEndAt(), game.getScheduledAt());
            game.changeScheduledEndAt(req.getScheduledEndAt());
        }

        // 5) 내용 (PUBLISHED 는 1번에서 수정 차단됨)
        if (req.hasContentField()) {
            game.editFields(
                    req.getOptionA(), req.getOptionB(),
                    req.getOptionADesc(), req.getOptionBDesc(),
                    req.getOptionAEmoji(), req.getOptionBEmoji(),
                    req.getDescription(), req.getCategoryCode()
            );
        }

        return AdminBalGameDetailRspDto.from(game);
    }

    // 상태 전환 매트릭스 검증.
    private void validateTransition(BalGameStatus from, BalGameStatus to, AdminBalGameUpdateReqDto req) {
        switch (from) {
            case DRAFT -> {
                if (to == BalGameStatus.HIDDEN) {
                    throw new BusinessException(ErrorCode.BAL_GAME_INVALID_TRANSITION);
                }
                if (to == BalGameStatus.SCHEDULED) {
                    LocalDateTime sched = resolveScheduledAtAfter(req);
                    if (sched == null || !sched.isAfter(LocalDateTime.now())) {
                        throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
                    }
                }
            }
            case SCHEDULED -> {
                if (to == BalGameStatus.HIDDEN || to == BalGameStatus.ARCHIVED) {
                    throw new BusinessException(ErrorCode.BAL_GAME_INVALID_TRANSITION);
                }
            }
            case PUBLISHED -> {
                if (to == BalGameStatus.DRAFT || to == BalGameStatus.SCHEDULED) {
                    throw new BusinessException(ErrorCode.BAL_GAME_INVALID_TRANSITION);
                }
            }
            case HIDDEN -> {
                if (to == BalGameStatus.DRAFT || to == BalGameStatus.SCHEDULED) {
                    throw new BusinessException(ErrorCode.BAL_GAME_INVALID_TRANSITION);
                }
            }
            case ARCHIVED -> {
                if (to == BalGameStatus.DRAFT || to == BalGameStatus.HIDDEN) {
                    throw new BusinessException(ErrorCode.BAL_GAME_INVALID_TRANSITION);
                }
                if (to == BalGameStatus.SCHEDULED) {
                    LocalDateTime sched = resolveScheduledAtAfter(req);
                    if (sched == null || !sched.isAfter(LocalDateTime.now())) {
                        throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
                    }
                }
            }
            default -> throw new BusinessException(ErrorCode.BAL_GAME_INVALID_TRANSITION);
        }
    }

    // 전환 후 scheduledAt 값 — 요청에 명시되면 그 값, 아니면 (clear 가 아니면) 검증 통과 위해 null 반환.
    // 검증 호출자에서 null 이면 INVALID_SCHEDULE 던짐.
    private LocalDateTime resolveScheduledAtAfter(AdminBalGameUpdateReqDto req) {
        if (Boolean.TRUE.equals(req.getClearScheduledAt())) return null;
        return req.getScheduledAt();
    }

    // 단일 scheduledAt 변경 검증 — SCHEDULED 상태면 미래여야 함
    private void validateScheduledAt(LocalDateTime scheduledAt, BalGameStatus currentStatus) {
        if (currentStatus == BalGameStatus.SCHEDULED && !scheduledAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
        }
    }

    // scheduledEndAt 검증 — 과거 거부 + (가능한 경우) scheduledAt 보다 미래여야 함.
    private void validateScheduledEndAt(LocalDateTime scheduledEndAt, LocalDateTime currentScheduledAt) {
        if (!scheduledEndAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
        }
        if (currentScheduledAt != null && !scheduledEndAt.isAfter(currentScheduledAt)) {
            throw new BusinessException(ErrorCode.BAL_GAME_INVALID_SCHEDULE);
        }
    }

    // 댓글 목록 - 숨김/삭제 모두 노출
    @Transactional(readOnly = true)
    public Page<AdminBalCommentRspDto> getComments(String gameUuid, Pageable pageable) {
        BalGame game = balGameRepository.findByUuid(gameUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_GAME));
        Long gameId = game.getId();

        Page<BalGameComment> page = balGameCommentRepository.findByGameId(gameId, pageable);
        if (page.isEmpty()) return page.map(c -> AdminBalCommentRspDto.of(c, null, null));

        List<BalGameComment> comments = page.getContent();

        Set<Long> userIds = comments.stream()
                .map(c -> c.getUser() == null ? null : c.getUser().getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
                : userRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        // voteChoice batch — 댓글 작성자들이 이 게임에 한 투표
        Map<Long, BalVoteChoice> voteByUserId = userIds.isEmpty() ? Map.of()
                : balVoteRepository.findByGameIdAndUserIdIn(gameId, userIds).stream()
                        .collect(Collectors.toMap(v -> v.getUser().getId(), BalVote::getChoice));

        return page.map(c -> {
            Long uid = c.getUser() == null ? null : c.getUser().getId();
            String nickname = uid == null ? null
                    : Optional.ofNullable(userMap.get(uid)).map(User::getNickname).orElse(null);
            BalVoteChoice choice = uid == null ? null : voteByUserId.get(uid);
            return AdminBalCommentRspDto.of(c, nickname, choice);
        });
    }
}
