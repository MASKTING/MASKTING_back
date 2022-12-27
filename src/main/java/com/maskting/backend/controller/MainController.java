package com.maskting.backend.controller;

import com.maskting.backend.dto.request.FeedRequest;
import com.maskting.backend.dto.request.SendLikeRequest;
import com.maskting.backend.service.MainService;
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

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal User user) throws IOException {
        return ResponseEntity.ok(mainService.getUser(user));
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(mainService.getFeed(user));
    }

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
