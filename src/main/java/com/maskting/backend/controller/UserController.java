package com.maskting.backend.controller;

import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.SignupRequest;
import com.maskting.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(String nickname) {
        return ResponseEntity.ok(userService.checkNickname(nickname));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid SignupRequest signupRequest,
                                    HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = userService.joinUser(signupRequest);
        userService.returnAccessToken(response, user);
        userService.returnRefreshToken(request, response, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.deleteAuth(request, response);
        return ResponseEntity.ok().build();
    }
}
