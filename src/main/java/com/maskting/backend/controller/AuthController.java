package com.maskting.backend.controller;

import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.User;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final AuthService authService;

    @PostMapping("/silent-refresh")
    public ResponseEntity<?> silentRefresh(HttpServletRequest request, HttpServletResponse response) {
        RefreshToken refreshToken = authService.getRefreshToken(request);
        authService.setAccessToken(response, refreshToken);
        return ResponseEntity.ok().build();
    }

}

