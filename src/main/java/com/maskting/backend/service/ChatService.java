package com.maskting.backend.service;

import com.maskting.backend.domain.ChatMessage;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.ChatMessageRequest;
import com.maskting.backend.repository.ChatMessageRepository;
import com.maskting.backend.repository.ChatRoomRepository;
import com.maskting.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessage saveChatMessage(ChatMessageRequest message) {
        return chatMessageRepository.save(buildChatMessage(message));
    }

    private ChatMessage buildChatMessage(ChatMessageRequest message) {
        return ChatMessage.builder()
                .chatRoom(chatRoomRepository.findById(message.getRoomId()).orElseThrow())
                .user(findUser(message))
                .content(message.getMessage())
                .build();
    }

    private User findUser(ChatMessageRequest message) {
        if (message.getSender().equals("System")) {
            return null;
        }
        return userRepository.findByNickname(message.getSender()).orElseThrow();
    }
}
