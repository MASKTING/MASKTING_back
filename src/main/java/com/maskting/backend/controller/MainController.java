package com.maskting.backend.controller;

import com.maskting.backend.dto.request.FeedRequest;
import com.maskting.backend.dto.request.SendLikeRequest;
import com.maskting.backend.service.MainService;
import com.maskting.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MainController {

    private final MainService mainService;
    private final JwtUtil jwtUtil;

    @PostMapping("/feed")
    public ResponseEntity<?> addFeed(@AuthenticationPrincipal User user, FeedRequest feedRequest) throws IOException {
        mainService.addFeed(user, feedRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/partner")
    public ResponseEntity<?> getPartner(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mainService.getPartnerResponse(mainService.matchPartner(user)));
    }

    @PostMapping("/like")
    public ResponseEntity<?> sendLike(@AuthenticationPrincipal User user, @RequestBody SendLikeRequest sendLikeRequest) {
        mainService.sendLike(user, sendLikeRequest.getNickname());
        return ResponseEntity.ok().build();
    }

}
