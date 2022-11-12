package com.maskting.backend.controller;

import com.maskting.backend.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/rooms")
    public ResponseEntity<?> getAllChatRoom(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatRoomService.getAllChatRoom(user));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getChatRoom(@PathVariable Long roomId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatRoomService.getChatRoom(roomId, user));
    }

}