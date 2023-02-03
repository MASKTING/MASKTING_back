package com.maskting.backend.controller;

import com.maskting.backend.dto.request.FinalDecisionRequest;
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

    @PostMapping("/room/{roomId}/out")
    public ResponseEntity<?> updateChatMessage(@PathVariable Long roomId, @AuthenticationPrincipal User user) {
        chatRoomService.updateChatMessage(roomId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/follower")
    public ResponseEntity<?> getFollowers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatRoomService.getFollowers(user));
    }

    @PostMapping("/reject/{nickname}")
    public ResponseEntity<?> rejectFollower(@PathVariable String nickname, @AuthenticationPrincipal User user) {
        chatRoomService.rejectFollower(nickname, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/profiles")
    public ResponseEntity<?> getFinalProfiles(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(chatRoomService.getFinalProfiles(user));
    }

    @PostMapping("/room/{roomId}/decision")
    public ResponseEntity<?> decideFinalDecision(@PathVariable Long roomId, @AuthenticationPrincipal User user,
                                                 @RequestBody FinalDecisionRequest finalDecisionRequest) {
        chatRoomService.decideFinalDecision(roomId, user, finalDecisionRequest);
        return ResponseEntity.ok().build();
    }
}
