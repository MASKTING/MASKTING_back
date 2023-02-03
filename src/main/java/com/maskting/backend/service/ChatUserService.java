package com.maskting.backend.service;

import com.maskting.backend.domain.ChatRoom;
import com.maskting.backend.domain.ChatUser;
import com.maskting.backend.domain.ChatUserDecision;
import com.maskting.backend.domain.User;
import com.maskting.backend.repository.ChatUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatUserService {

    private final ChatUserRepository chatUserRepository;

    public ChatUser createChatUser(User user, ChatRoom chatRoom) {
        return chatUserRepository.save(buildChatUser(user, chatRoom));
    }

    private ChatUser buildChatUser(User user, ChatRoom chatRoom) {
        return ChatUser.builder()
                .user(user)
                .chatRoom(chatRoom)
                .decision(ChatUserDecision.STILL)
                .build();
    }

}
