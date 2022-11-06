package com.maskting.backend.controller;

import com.maskting.backend.dto.request.ChatMessageRequest;
import com.maskting.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(ChatMessageRequest message) {
        chatService.saveChatMessage(message);
        chatService.sendMessage(message);
    }
}
