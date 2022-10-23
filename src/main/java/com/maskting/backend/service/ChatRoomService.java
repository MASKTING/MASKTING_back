package com.maskting.backend.service;

import com.maskting.backend.domain.ChatRoom;
import com.maskting.backend.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom createRoom() {
        return chatRoomRepository.save(new ChatRoom());
    }
}
