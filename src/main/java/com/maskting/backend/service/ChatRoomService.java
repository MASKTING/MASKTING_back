package com.maskting.backend.service;

import com.maskting.backend.domain.ChatMessage;
import com.maskting.backend.domain.ChatRoom;
import com.maskting.backend.domain.ChatUser;
import com.maskting.backend.dto.response.ChatRoomsResponse;
import com.maskting.backend.repository.ChatRoomRepository;
import com.maskting.backend.repository.ChatUserRepository;
import com.maskting.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
    private final static int DAY = 24;

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatUserRepository chatUserRepository;

    public ChatRoom createRoom() {
        return chatRoomRepository.save(new ChatRoom());
    }

    public List<ChatRoomsResponse> getAllChatRoom(User userDetail) {
        com.maskting.backend.domain.User user = userRepository.findByProviderId(userDetail.getUsername());
        List<ChatRoomsResponse> chatRoomResponses = new ArrayList<>();
        List<Long> chatRoomId = getChatRoomId(user);
        
        addChatRoomsResponse(user, chatRoomResponses, chatRoomId);
        return chatRoomResponses;
    }

    private void addChatRoomsResponse(com.maskting.backend.domain.User user, List<ChatRoomsResponse> chatRoomResponses, List<Long> chatRoomId) {
        for (Long id : chatRoomId) {
            ChatRoom chatRoom = chatRoomRepository.findById(id).orElseThrow();
            com.maskting.backend.domain.User partner = getPartner(user.getId(), chatRoom);

            chatRoomResponses.add(buildChatRoomsResponse(id, chatRoom, partner));
        }
    }

    private ChatRoomsResponse buildChatRoomsResponse(Long id, ChatRoom chatRoom, com.maskting.backend.domain.User partner) {
        ChatMessage chatMessage = getLastChatMessage(chatRoom);

        return ChatRoomsResponse.builder()
                .profile(partner.getProfiles().get(0).getPath())
                .roomId(id)
                .roomName(partner.getNickname())
                .remainingTime(getRemainingTime(chatRoom))
                .lastMessage(chatMessage.getContent())
                .lastUpdatedAt(getLastUpdatedAt(chatMessage))
                .build();
    }

    private String getLastUpdatedAt(ChatMessage chatMessage) {
        LocalDateTime createdAt = chatMessage.getCreatedAt();
        long until = createdAt.until(LocalDateTime.now(), ChronoUnit.HOURS);
        if (until < DAY) {
            return until + "시간 전";
        }
        
        return createdAt.getYear() + "-" + createdAt.getMonth() + "-" + createdAt.getDayOfMonth();
    }

    private List<Long> getChatRoomId(com.maskting.backend.domain.User user) {
        return chatUserRepository
                .findAllByUserId(user.getId())
                .stream()
                .map(ChatUser::getChatRoom)
                .map(ChatRoom::getId)
                .collect(Collectors.toList());
    }

    private ChatMessage getLastChatMessage(ChatRoom chatRoom) {
        return chatRoom.getChatMessages()
                .stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt).reversed())
                .findFirst()
                .get();
    }

    private int getRemainingTime(ChatRoom chatRoom) {
        return (3 * DAY) - (int) chatRoom.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.HOURS);
    }

    private com.maskting.backend.domain.User getPartner(Long id, ChatRoom chatRoom) {
        return chatRoom.getChatUsers()
                .stream()
                .filter(r -> r.getId() != id)
                .findFirst()
                .orElseThrow()
                .getUser();
    }

}
