package com.maskting.backend.controller;

import com.maskting.backend.dto.request.FeedRequest;
import com.maskting.backend.dto.request.SendLikeRequest;
import com.maskting.backend.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MainController {

    private final MainService mainService;

    @PostMapping("/feed")
    public ResponseEntity<?> addFeed(HttpServletRequest request, FeedRequest feedRequest) throws IOException {
        mainService.addFeed(request, feedRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/partner")
    public ResponseEntity<?> getPartner(HttpServletRequest request) {
        return ResponseEntity.ok(mainService.getPartnerResponse(mainService.matchPartner(request)));
    }

    @PostMapping("/like")
    public ResponseEntity<?> sendLike(HttpServletRequest request, @RequestBody SendLikeRequest sendLikeRequest) {
        mainService.sendLike(request, sendLikeRequest.getNickname());
        return ResponseEntity.ok().build();
    }
}
