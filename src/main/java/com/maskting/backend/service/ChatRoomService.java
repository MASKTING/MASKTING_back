package com.maskting.backend.service;

import com.maskting.backend.domain.*;
import com.maskting.backend.dto.response.ChatMessageResponse;
import com.maskting.backend.dto.response.ChatRoomResponse;
import com.maskting.backend.dto.response.ChatRoomsResponse;
import com.maskting.backend.repository.ChatRoomRepository;
import com.maskting.backend.repository.ChatUserRepository;
import com.maskting.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final static int NOON = 12;

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final ChatUserRepository chatUserRepository;

    public ChatRoom createRoom() {
        return chatRoomRepository.save(new ChatRoom());
    }

    public List<ChatRoomsResponse> getAllChatRoom(User userDetail) {
        com.maskting.backend.domain.User user = getUser(userDetail);
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
        int remainingTime = Math.max(getRemainingTime(chatRoom), 0);

        return ChatRoomsResponse.builder()
                .profile(partner.getProfiles().get(0).getPath())
                .roomId(id)
                .roomName(partner.getNickname())
                .remainingTime(remainingTime)
                .lastMessage(chatMessage.getContent())
                .lastUpdatedAt(getLastUpdatedAt(chatMessage))
                .update(isChatRoomUpdate(chatRoom, partner))
                .build();
    }

    private boolean isChatRoomUpdate(ChatRoom chatRoom, com.maskting.backend.domain.User partner) {
        return chatRoom.getChatMessages().stream()
                .anyMatch(chatMessage1 -> (chatMessage1.getUser().getId() == partner.getId()) && !chatMessage1.isChecked());
    }

    private String getLastUpdatedAt(ChatMessage chatMessage) {
        LocalDateTime createdAt = chatMessage.getCreatedAt();
        long until = createdAt.until(LocalDateTime.now(), ChronoUnit.HOURS);
        if (until < 1) {
            return createdAt.until(LocalDateTime.now(), ChronoUnit.MINUTES) + "분 전";
        }
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
                .filter(r -> r.getUser().getId() != id)
                .findFirst()
                .orElseThrow()
                .getUser();
    }

    @Transactional
    public ChatRoomResponse getChatRoom(Long roomId, User userDetail) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow();
        com.maskting.backend.domain.User user = getUser(userDetail);
        com.maskting.backend.domain.User partner = getPartner(user.getId(), chatRoom);
        List<ChatMessage> chatMessages = getChatMessages(chatRoom);
        List<ChatMessageResponse> chatRoomResponses = new ArrayList<>();

        updateChatMessage(chatRoom, user);
        addChatRoomResponse(chatMessages, chatRoomResponses);
        return buildChatRoomResponse(chatRoom, partner, chatRoomResponses);
    }

    private void updateChatMessage(ChatRoom chatRoom, com.maskting.backend.domain.User user) {
        List<ChatMessage> chatMessages = getPartnerChatMessage(chatRoom, user);
        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.updateChecked();
        }
    }

    private List<ChatMessage> getPartnerChatMessage(ChatRoom chatRoom, com.maskting.backend.domain.User user) {
        return chatRoom.getChatMessages()
                .stream()
                .filter(chatMessage -> (chatMessage.getUser().getId() != user.getId() && !chatMessage.isChecked()))
                .collect(Collectors.toList());
    }

    private void addChatRoomResponse(List<ChatMessage> chatMessages, List<ChatMessageResponse> chatRoomResponses) {
        for (int i = 0; i < chatMessages.size(); i++) {
            chatRoomResponses.add(buildChatMessageResponse(chatMessages, i, getCreatedAt(chatMessages, i)));
        }
    }

    private ChatRoomResponse buildChatRoomResponse(ChatRoom chatRoom, com.maskting.backend.domain.User partner, List<ChatMessageResponse> chatRoomResponses) {
        return ChatRoomResponse.builder()
                .profile(partner.getProfiles().get(0).getPath())
                .roomName(partner.getNickname())
                .remainingTime(getRemainingTime(chatRoom))
                .messages(chatRoomResponses)
                .build();
    }

    private ChatMessageResponse buildChatMessageResponse(List<ChatMessage> chatMessages, int i, StringBuilder createdAt) {
        return ChatMessageResponse.builder()
                .nickname(getNicknames(chatMessages).get(i))
                .content(getContents(chatMessages).get(i))
                .createdAt(createdAt.toString()).build();
    }

    private StringBuilder getCreatedAt(List<ChatMessage> chatMessages, int i) {
        StringBuilder createdAt = new StringBuilder();
        LocalTime localTime = getCreatedTimes(chatMessages).get(i).toLocalTime();
        int hour = localTime.getHour() + 9;

        createdAt.append(getType(hour) + " " + passNoon(hour) + ":" + getMinute(localTime));
        return createdAt;
    }

    private String getMinute(LocalTime localTime) {
        StringBuilder minute = new StringBuilder();
        if (localTime.getMinute() < 10){
            minute.append("0");
        }
        minute.append(localTime.getMinute());
        return minute.toString();
    }

    private List<LocalDateTime> getCreatedTimes(List<ChatMessage> chatMessages) {
        return chatMessages
                .stream()
                .map(BaseTimeEntity::getCreatedAt)
                .collect(Collectors.toList());
    }

    private List<String> getContents(List<ChatMessage> chatMessages) {
        return chatMessages
                .stream()
                .map(ChatMessage::getContent)
                .collect(Collectors.toList());
    }

    private List<String> getNicknames(List<ChatMessage> chatMessages) {
        return chatMessages
                .stream()
                .map(ChatMessage::getUser)
                .map(com.maskting.backend.domain.User::getNickname)
                .collect(Collectors.toList());
    }

    private List<ChatMessage> getChatMessages(ChatRoom chatRoom) {
        return chatRoom.getChatMessages()
                .stream()
                .sorted(Comparator.comparingLong(ChatMessage::getId))
                .collect(Collectors.toList());
    }

    private String getType(int hour) {
        if (hour >= NOON)
            return "오후";
        return "오전";
    }

    private int passNoon(int hour) {
        if (hour > NOON) {
            hour -= NOON;
        }
        return hour;
    }

    private com.maskting.backend.domain.User getUser(User userDetail) {
        return userRepository.findByProviderId(userDetail.getUsername());
    }
}
