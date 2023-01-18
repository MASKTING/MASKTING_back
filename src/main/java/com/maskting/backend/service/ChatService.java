package com.maskting.backend.service;

import com.maskting.backend.domain.ChatMessage;
import com.maskting.backend.domain.ChatRoom;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.ChatMessageRequest;
import com.maskting.backend.repository.ChatMessageRepository;
import com.maskting.backend.repository.ChatRoomRepository;
import com.maskting.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessage saveChatMessage(ChatMessageRequest messageRequest) {
        ChatRoom chatRoom = chatRoomRepository.findById(messageRequest.getRoomId()).orElseThrow();
        ChatMessage message = chatMessageRepository.save(buildChatMessage(messageRequest, chatRoom));
        chatRoom.addMessage(message);
        return message;
    }

    private ChatMessage buildChatMessage(ChatMessageRequest message, ChatRoom chatRoom) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .user(findUser(message))
                .content(message.getMessage())
                .build();
    }

    private User findUser(ChatMessageRequest message) {
        return userRepository.findByNickname(message.getSender()).orElseThrow();
    }

    public void sendMessage(ChatMessageRequest message) {
        simpMessagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}
