package com.maskting.backend.controller;

import com.maskting.backend.dto.request.FeedRequest;
import com.maskting.backend.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        mainService.getPartnerResponse(mainService.matchPartner(request));
        return ResponseEntity.ok().build();
    }

}
