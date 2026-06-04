package com.nokcha.efbe.domain.chat.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.util.CursorCodec;
import com.nokcha.efbe.common.util.LocationUtil;
import com.nokcha.efbe.domain.chat.dto.request.ChatMemoReqDto;
import com.nokcha.efbe.domain.chat.dto.request.ChatReportLeaveReqDto;
import com.nokcha.efbe.domain.chat.dto.request.ChatRoomCreateReqDto;
import com.nokcha.efbe.domain.chat.dto.response.ChatMemoRspDto;
import com.nokcha.efbe.domain.chat.dto.response.ChatProfileOpenRspDto;
import com.nokcha.efbe.domain.chat.dto.response.ChatRoomRspDto;
import com.nokcha.efbe.domain.chat.entity.ChatParticipant;
import com.nokcha.efbe.domain.chat.entity.ChatReportEvidence;
import com.nokcha.efbe.domain.chat.entity.ChatRoom;
import com.nokcha.efbe.domain.chat.entity.ChatRoomType;
import com.nokcha.efbe.domain.chat.repository.ChatParticipantRepository;
import com.nokcha.efbe.domain.chat.repository.ChatReportEvidenceRepository;
import com.nokcha.efbe.domain.chat.repository.ChatRoomRepository;
import com.nokcha.efbe.domain.chat.repository.projection.ChatRoomCursor;
import com.nokcha.efbe.domain.postIt.dto.request.PostReplyReqDto;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.repository.PostItRepository;
import com.nokcha.efbe.domain.profile.entity.CodeKeyword;
import com.nokcha.efbe.domain.profile.entity.CodePersonal;
import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.UserCustomKeyword;
import com.nokcha.efbe.domain.profile.entity.UserKeyword;
import com.nokcha.efbe.domain.profile.entity.UserPersonal;
import com.nokcha.efbe.domain.profile.entity.UserPersonalType;
import com.nokcha.efbe.domain.profile.entity.UserProfile;
import com.nokcha.efbe.domain.profile.repository.ProfileRepository;
import com.nokcha.efbe.domain.profile.repository.UserCustomKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserPersonalRepository;
import com.nokcha.efbe.domain.area.repository.AreaRepository;
import com.nokcha.efbe.domain.report.dto.response.ReportRspDto;
import com.nokcha.efbe.domain.report.entity.Report;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import com.nokcha.efbe.domain.report.service.ReportService;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import com.nokcha.efbe.domain.user.repository.CodeKeywordRepository;
import com.nokcha.efbe.domain.user.repository.CodePersonalRepository;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int DEFAULT_ROOM_SIZE = 20;
    private static final int MAX_ROOM_SIZE = 50;
    private static final int MATCH_ROOM_REUSE_DAYS = 30;
    private static final List<ChatRoomType> MATCH_CHAT_ROOM_TYPES = List.of(ChatRoomType.MATCH, ChatRoomType.POWER_MESSAGE);

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatReportEvidenceRepository chatReportEvidenceRepository;
    private final UserRepository userRepository;
    private final PostItRepository postItRepository;
    private final ReportService reportService;
    private final ProfileRepository profileRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final UserCustomKeywordRepository userCustomKeywordRepository;
    private final UserPersonalRepository userPersonalRepository;
    private final CodeKeywordRepository codeKeywordRepository;
    private final CodePersonalRepository codePersonalRepository;
    private final AreaRepository areaRepository;
    private final CursorCodec cursorCodec;

    // 내 채팅방 목록
    @Transactional(readOnly = true)
    public CursorPageResponse<ChatRoomRspDto> getMyRooms(Long userId, String cursor, Integer size) {
        int pageSize = clampSize(size);
        ChatRoomCursor decoded = cursorCodec.decode(cursor, ChatRoomCursor.class);
        validateCursor(decoded);

        List<ChatRoom> rows = decoded == null
                ? chatRoomRepository.findMyRooms(userId, PageRequest.of(0, pageSize + 1))
                : chatRoomRepository.findMyRoomsAfterCursor(userId, decoded.sortAt(), decoded.id(), PageRequest.of(0, pageSize + 1));

        boolean hasMore = rows.size() > pageSize;
        List<ChatRoom> page = hasMore ? rows.subList(0, pageSize) : rows;
        List<ChatRoomRspDto> items = page.stream().map(ChatRoomRspDto::from).toList();
        if (!hasMore) return CursorPageResponse.last(items);

        ChatRoom tail = page.getLast();
        String nextCursor = cursorCodec.encode(new ChatRoomCursor(resolveRoomSortAt(tail), tail.getId()));
        return CursorPageResponse.of(items, nextCursor);
    }

    // 포스트잇 답장
    @Transactional
    public ChatRoomRspDto replyToPost(Long postId, Long partnerId, PostReplyReqDto req) {
        PostIt post = postItRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        if (Boolean.TRUE.equals(post.getIsDeleted()) || Boolean.TRUE.equals(post.getIsHidden())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_POST);
        }
        if (post.isExpired()) throw new BusinessException(ErrorCode.POST_EXPIRED);
        if (post.getUser().getStatus() == UserStatus.WITHDRAWN) throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        if (post.getUser().getId().equals(partnerId)) {
            throw new BusinessException(ErrorCode.SELF_ACTION_FORBIDDEN);
        }

        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        if (partner.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_USER);
        }

        // 이미 답장한 포스트잇이면 예외
        Pair pair = Pair.of(post.getUser().getId(), partner.getId());
        if (chatParticipantRepository.existsByChatRoom_RoomTypeAndChatRoom_Post_IdAndUser_IdAndLeftAtIsNull(
                ChatRoomType.POST, post.getId(), partnerId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_POST_REPLY);
        }

        return createPostChatRoom(post, partner, pair, req);
    }

    // 채팅방 생성
    @Transactional
    public ChatRoomRspDto createRoom(Long currentUserId, ChatRoomCreateReqDto reqDto) {
        User currentUser = getActiveUser(currentUserId);

        return switch (reqDto.getRoomType()) {
            case POST -> throw new BusinessException(ErrorCode.INVALID_REQUEST);
            case MATCH -> createMatchRoom(currentUser, reqDto);
            case POWER_MESSAGE -> createPowerMessageRoom(currentUser, reqDto);
        };
    }

    // 채팅방 나가기 - 요청한 참여자만 목록에서 제외
    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CHAT_ROOM));

        if (!Boolean.TRUE.equals(room.getIsActive())) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }

        ChatParticipant participant = chatParticipantRepository.findByChatRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT));
        if (participant.hasLeft()) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }

        participant.leave(LocalDateTime.now());

        // 두 명 다 나가는 경우 채팅방 비활성화
        if (!chatParticipantRepository.existsByChatRoom_IdAndLeftAtIsNull(roomId)) {
            room.deactivate();
        }
    }

    // 채팅 신고 후 나가기
    @Transactional
    public ReportRspDto reportAndLeaveRoom(Long roomId, Long userId, ChatReportLeaveReqDto reqDto) {
        ChatRoom room = getActiveRoom(roomId);
        ChatParticipant participant = chatParticipantRepository.findByChatRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT));
        if (participant.hasLeft()) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }

        Report report = reportService.createReportEntity(userId, ReportTargetType.CHAT, roomId, null, null);
        saveChatReportEvidences(report, room, reqDto.getMessages());

        participant.leave(LocalDateTime.now());
        if (!chatParticipantRepository.existsByChatRoom_IdAndLeftAtIsNull(roomId)) {
            room.deactivate();
        }

        return ReportRspDto.from(report);
    }

    // 채팅방 메모 수정
    @Transactional
    public ChatMemoRspDto updateMemo(Long roomId, Long userId, ChatMemoReqDto reqDto) {
        getActiveRoom(roomId);
        ChatParticipant participant = chatParticipantRepository.findByChatRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT));
        if (participant.hasLeft()) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }

        participant.updateMemo(trimToNull(reqDto.getMemo()));
        return ChatMemoRspDto.from(participant);
    }

    // 채팅 프로필 오픈 단계 증가
    @Transactional
    public ChatProfileOpenRspDto advanceProfileOpenLevel(Long roomId, Long userId) {
        ChatRoom room = getActiveRoom(roomId);
        validateProfileOpenSupported(room);

        ChatParticipant requester = chatParticipantRepository.findByChatRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT));
        if (requester.hasLeft()) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }

        chatParticipantRepository.findByChatRoom_Id(roomId).stream()
                .filter(participant -> !participant.hasLeft())
                .forEach(ChatParticipant::advanceProfileOpenLevel);

        return getProfileOpen(roomId, userId);
    }

    // 채팅방 상대 프로필 공개 정보 조회
    @Transactional(readOnly = true)
    public ChatProfileOpenRspDto getProfileOpen(Long roomId, Long userId) {
        ChatRoom room = getActiveRoom(roomId);
        validateProfileOpenSupported(room);

        ChatParticipant viewerParticipant = chatParticipantRepository.findByChatRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT));
        if (viewerParticipant.hasLeft()) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }

        Long targetUserId = resolveTargetUserId(room, userId);
        ChatParticipant targetParticipant = chatParticipantRepository.findByChatRoom_IdAndUser_Id(roomId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT));

        int level = normalizeProfileOpenLevel(viewerParticipant.getProfileOpenLevel());

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        UserProfile targetProfile = profileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROFILE));

        List<String> sectionOrder = resolveSectionOrder(targetProfile);
        boolean keywordOpen = isSectionOpen(sectionOrder, "KEYWORD", level);
        boolean idealOpen = isSectionOpen(sectionOrder, "IDEAL", level);
        boolean fullOpen = level >= 4;

        Map<UserPersonalType, Map<String, List<String>>> personalProfiles = groupPersonalProfiles(targetUserId);

        return ChatProfileOpenRspDto.builder()
                .chatRoomId(room.getId())
                .targetUserId(targetUserId)
                .anonymous(false)
                .profileOpenLevel(level)
                .sectionOrder(sectionOrder)
                .basicProfile(buildBasicProfile(targetParticipant, targetUser))
                .keywordProfile(keywordOpen ? buildKeywordProfile(targetUserId) : null)
                .idealProfile(idealOpen ? personalProfiles.getOrDefault(UserPersonalType.IDEAL, Map.of()) : null)
                .fullProfile(fullOpen ? buildFullProfile(targetProfile, personalProfiles.getOrDefault(UserPersonalType.SELF, Map.of())) : null)
                .build();
    }

    private ChatRoomRspDto createPostChatRoom(PostIt post, User partner, Pair pair, PostReplyReqDto req) {
        LocalDateTime now = LocalDateTime.now();
        String content = req.getContent().trim();
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
                .uuid(UUID.randomUUID().toString())
                .firebaseId("chat_" + UUID.randomUUID())
                .roomType(ChatRoomType.POST)
                .post(post)
                .postContentSnapshot(post.getContent())
                .pairUserAId(pair.userAId())
                .pairUserBId(pair.userBId())
                .isAnonymous(post.getIsAnonymous())
                .lastMessage(content)
                .lastMessageAt(now)
                .build());

        saveParticipants(chatRoom, post.getUser(), partner);
        post.increaseReplyCount();
        return ChatRoomRspDto.from(chatRoom);
    }

    private void saveChatReportEvidences(Report report, ChatRoom room, List<ChatReportLeaveReqDto.MessageEvidence> messages) {
        if (messages == null || messages.isEmpty()) return;

        List<ChatReportEvidence> evidences = messages.stream()
                .filter(Objects::nonNull)
                .filter(this::hasEvidenceValue)
                .map(message -> {
                    validateMessageSender(room, message.getSenderUserId());
                    return ChatReportEvidence.builder()
                            .report(report)
                            .chatRoom(room)
                            .firebaseMessageId(trimToNull(message.getFirebaseMessageId()))
                            .senderUserId(message.getSenderUserId())
                            .contentSnapshot(trimToNull(message.getContentSnapshot()))
                            .sentAt(message.getSentAt())
                            .build();
                })
                .toList();

        if (!evidences.isEmpty()) chatReportEvidenceRepository.saveAll(evidences);
    }

    private boolean hasEvidenceValue(ChatReportLeaveReqDto.MessageEvidence message) {
        return trimToNull(message.getFirebaseMessageId()) != null
                || trimToNull(message.getContentSnapshot()) != null
                || message.getSenderUserId() != null
                || message.getSentAt() != null;
    }

    private void validateMessageSender(ChatRoom room, Long senderUserId) {
        if (senderUserId == null) return;
        if (!room.getPairUserAId().equals(senderUserId) && !room.getPairUserBId().equals(senderUserId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private ChatRoomRspDto createMatchRoom(User currentUser, ChatRoomCreateReqDto reqDto) {
        if (reqDto.getTargetUserId() == null || reqDto.getMatchResultId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        User targetUser = getActiveUser(reqDto.getTargetUserId());
        validateOtherUser(currentUser.getId(), targetUser.getId());
        Pair pair = Pair.of(currentUser.getId(), targetUser.getId());

        ChatRoom reusableRoom = findReusableMatchChatRoom(pair);
        if (reusableRoom != null) {
            rejoinParticipants(reusableRoom, currentUser, targetUser);
            return ChatRoomRspDto.from(reusableRoom);
        }

        ChatRoom room = chatRoomRepository.save(ChatRoom.builder()
                .uuid(generateUuid())
                .firebaseId(resolveFirebaseId(reqDto))
                .roomType(ChatRoomType.MATCH)
                .matchResultId(reqDto.getMatchResultId())
                .pairUserAId(pair.userAId())
                .pairUserBId(pair.userBId())
                .build());

        saveParticipants(room, currentUser, targetUser);
        return ChatRoomRspDto.from(room);
    }

    private ChatRoomRspDto createPowerMessageRoom(User currentUser, ChatRoomCreateReqDto reqDto) {
        if (reqDto.getTargetUserId() == null || reqDto.getPowerMessage() == null || reqDto.getPowerMessage().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        User targetUser = getActiveUser(reqDto.getTargetUserId());
        validateOtherUser(currentUser.getId(), targetUser.getId());
        Pair pair = Pair.of(currentUser.getId(), targetUser.getId());

        ChatRoom reusableRoom = findReusableMatchChatRoom(pair);
        if (reusableRoom != null) {
            rejoinParticipants(reusableRoom, currentUser, targetUser);
            return ChatRoomRspDto.from(reusableRoom);
        }

        ChatRoom room = chatRoomRepository.save(ChatRoom.builder()
                .uuid(generateUuid())
                .firebaseId(resolveFirebaseId(reqDto))
                .roomType(ChatRoomType.POWER_MESSAGE)
                .powerMessage(reqDto.getPowerMessage().trim())
                .powerPinnedUntil(LocalDateTime.now().plusDays(3))
                .pairUserAId(pair.userAId())
                .pairUserBId(pair.userBId())
                .lastMessage(reqDto.getPowerMessage().trim())
                .lastMessageAt(LocalDateTime.now())
                .build());

        saveParticipants(room, currentUser, targetUser);
        return ChatRoomRspDto.from(room);
    }

    private ChatRoom findReusableMatchChatRoom(Pair pair) {
        return chatRoomRepository.findFirstByRoomTypeInAndPairUserAIdAndPairUserBIdAndIsActiveTrueOrderByCreateTimeDescIdDesc(
                        MATCH_CHAT_ROOM_TYPES, pair.userAId(), pair.userBId())
                .filter(room -> room.getCreateTime() != null)
                .filter(room -> !room.getCreateTime().isBefore(LocalDateTime.now().minusDays(MATCH_ROOM_REUSE_DAYS)))
                .orElse(null);
    }

    private void rejoinParticipants(ChatRoom room, User firstUser, User secondUser) {
        rejoinParticipantIfPresent(room, firstUser);
        rejoinParticipantIfPresent(room, secondUser);
    }

    private void rejoinParticipantIfPresent(ChatRoom room, User user) {
        chatParticipantRepository.findByChatRoom_IdAndUser_Id(room.getId(), user.getId())
                .filter(ChatParticipant::hasLeft)
                .ifPresent(ChatParticipant::rejoin);
    }

    private void saveParticipants(ChatRoom room, User firstUser, User secondUser) {
        saveParticipantIfAbsent(room, firstUser);
        saveParticipantIfAbsent(room, secondUser);
    }

    private void saveParticipantIfAbsent(ChatRoom room, User user) {
        if (chatParticipantRepository.findByChatRoom_IdAndUser_Id(room.getId(), user.getId()).isPresent()) {
            return;
        }
        chatParticipantRepository.save(createParticipant(room, user));
    }

    private ChatParticipant createParticipant(ChatRoom room, User user) {
        return ChatParticipant.builder()
                .chatRoom(room)
                .user(user)
                .displayName(user.getNickname())
                .build();
    }

    private User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.INVALID_USER);
        }
        return user;
    }

    private void validateOtherUser(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.SELF_ACTION_FORBIDDEN);
        }
    }

    private String resolveFirebaseId(ChatRoomCreateReqDto reqDto) {
        if (reqDto.getFirebaseId() != null && !reqDto.getFirebaseId().isBlank()) {
            return reqDto.getFirebaseId().trim();
        }
        return "chat_" + generateUuid();
    }

    private String generateUuid() {
        return UUID.randomUUID().toString();
    }

    private int clampSize(Integer size) {
        if (size == null || size <= 0) return DEFAULT_ROOM_SIZE;
        if (size > MAX_ROOM_SIZE) throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        return size;
    }

    private LocalDateTime resolveRoomSortAt(ChatRoom room) {
        return room.getLastMessageAt() != null ? room.getLastMessageAt() : room.getCreateTime();
    }

    private void validateCursor(ChatRoomCursor cursor) {
        if (cursor != null && (cursor.sortAt() == null || cursor.id() == null)) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
    }

    private ChatRoom getActiveRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CHAT_ROOM));
        if (!Boolean.TRUE.equals(room.getIsActive())) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }
        return room;
    }

    private void validateProfileOpenSupported(ChatRoom room) {
        if (room.getRoomType() == ChatRoomType.MATCH) {
            return;
        }
        if (room.getRoomType() == ChatRoomType.POST && !Boolean.TRUE.equals(room.getIsAnonymous())) {
            return;
        }
        throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }

    private Long resolveTargetUserId(ChatRoom room, Long viewerUserId) {
        if (room.getPairUserAId().equals(viewerUserId)) {
            return room.getPairUserBId();
        }
        if (room.getPairUserBId().equals(viewerUserId)) {
            return room.getPairUserAId();
        }
        throw new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT);
    }

    private int normalizeProfileOpenLevel(Integer profileOpenLevel) {
        if (profileOpenLevel == null || profileOpenLevel < 1) {
            return 1;
        }
        return Math.min(profileOpenLevel, 4);
    }

    private List<String> resolveSectionOrder(UserProfile profile) {
        boolean keywordFirst = isKeywordBeforeIdeal(profile.getIdealPointTypes());
        if (keywordFirst) {
            return List.of("BASIC", "KEYWORD", "IDEAL", "FULL");
        }
        return List.of("BASIC", "IDEAL", "KEYWORD", "FULL");
    }

    private boolean isKeywordBeforeIdeal(List<IdealPointType> idealPointTypes) {
        if (idealPointTypes == null || idealPointTypes.isEmpty()) {
            return true;
        }

        int keywordIndex = idealPointTypes.indexOf(IdealPointType.KEYWORD);
        int idealIndex = idealPointTypes.indexOf(IdealPointType.IDEAL_TYPE);
        if (keywordIndex < 0 && idealIndex < 0) {
            return true;
        }
        if (keywordIndex < 0) {
            return false;
        }
        if (idealIndex < 0) {
            return true;
        }
        return keywordIndex <= idealIndex;
    }

    private boolean isSectionOpen(List<String> sectionOrder, String section, int level) {
        int sectionLevel = sectionOrder.indexOf(section) + 1;
        return sectionLevel > 0 && level >= sectionLevel;
    }

    private ChatProfileOpenRspDto.BasicProfile buildBasicProfile(ChatParticipant targetParticipant, User targetUser) {
        String area = targetUser.getAreaId() == null ? null : areaRepository.findById(targetUser.getAreaId())
                .map(LocationUtil::composeLocation)
                .orElse(null);

        return ChatProfileOpenRspDto.BasicProfile.builder()
                .nickname(targetParticipant.getDisplayName())
                .age(targetUser.getAge())
                .area(area)
                .build();
    }

    private ChatProfileOpenRspDto.KeywordProfile buildKeywordProfile(Long userId) {
        List<UserKeyword> userKeywords = userKeywordRepository.findByUserId(userId);
        List<Long> keywordIds = userKeywords.stream()
                .map(UserKeyword::getKeywordId)
                .toList();
        Map<Long, CodeKeyword> keywordMap = codeKeywordRepository.findAllById(keywordIds)
                .stream()
                .collect(Collectors.toMap(CodeKeyword::getId, Function.identity()));

        Map<String, List<String>> groupedKeywords = new LinkedHashMap<>();
        userKeywords.stream()
                .map(userKeyword -> keywordMap.get(userKeyword.getKeywordId()))
                .filter(keyword -> keyword != null)
                .forEach(keyword -> groupedKeywords
                        .computeIfAbsent(keyword.getBigCategory(), ignored -> new ArrayList<>())
                        .add(keyword.getSmallCategory()));

        List<String> customKeywords = userCustomKeywordRepository.findByUserId(userId)
                .stream()
                .map(UserCustomKeyword::getKeyword)
                .toList();

        return ChatProfileOpenRspDto.KeywordProfile.builder()
                .keywords(groupedKeywords)
                .customKeywords(customKeywords)
                .build();
    }

    private Map<UserPersonalType, Map<String, List<String>>> groupPersonalProfiles(Long userId) {
        List<UserPersonal> userPersonals = userPersonalRepository.findByUserId(userId);
        List<Long> personalIds = userPersonals.stream()
                .map(UserPersonal::getPersonalId)
                .toList();
        Map<Long, CodePersonal> personalMap = codePersonalRepository.findAllById(personalIds)
                .stream()
                .collect(Collectors.toMap(CodePersonal::getId, Function.identity()));

        Map<UserPersonalType, Map<String, List<String>>> result = new LinkedHashMap<>();
        for (UserPersonal userPersonal : userPersonals) {
            CodePersonal personal = personalMap.get(userPersonal.getPersonalId());
            if (personal == null) {
                continue;
            }

            result.computeIfAbsent(userPersonal.getType(), ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(personal.getBigCategory(), ignored -> new ArrayList<>())
                    .add(personal.getSmallCategory());
        }
        return result;
    }

    private ChatProfileOpenRspDto.FullProfile buildFullProfile(UserProfile profile, Map<String, List<String>> selfProfile) {
        return ChatProfileOpenRspDto.FullProfile.builder()
                .purpose(profile.getPurpose() == null ? null : profile.getPurpose().name())
                .job(profile.getJob() == null ? null : profile.getJob().name())
                .mbti(profile.getMbti() == null ? null : profile.getMbti().name())
                .bioMessage(profile.getBioMessage())
                .selfProfile(selfProfile == null ? Collections.emptyMap() : selfProfile)
                .build();
    }

    private record Pair(Long userAId, Long userBId) {
        private static Pair of(Long firstUserId, Long secondUserId) {
            return firstUserId < secondUserId ? new Pair(firstUserId, secondUserId) : new Pair(secondUserId, firstUserId);
        }
    }
}
