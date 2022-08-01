package com.maskting.backend.controller;

import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.AdditionalSignupRequest;
import com.maskting.backend.dto.request.SignupRequest;
import com.maskting.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest,
                                    HttpServletRequest request, HttpServletResponse response) {
        User user = userService.joinUser(signupRequest);
        userService.returnAccessToken(response, user);
        userService.returnRefreshToken(request, response, user);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/additional-signup")
    public ResponseEntity<?> additionalSignup(AdditionalSignupRequest additionalSignupRequest) throws IOException {
        userService.addAdditionalInfo(additionalSignupRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.deleteAuth(request, response);
        return ResponseEntity.ok().build();
    }
}
