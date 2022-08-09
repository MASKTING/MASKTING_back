package com.maskting.backend.service;

import com.maskting.backend.domain.User;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public User getUser(HttpServletRequest request) {
        String accessToken = jwtUtil.resolveToken(request);
        String providerId = jwtUtil.getSubject(accessToken);
        User user = userRepository.findByProviderId(providerId);
        return user;
    }
}
