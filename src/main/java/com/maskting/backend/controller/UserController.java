package com.maskting.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.CheckSmsRequest;
import com.maskting.backend.dto.request.ReSignupRequest;
import com.maskting.backend.dto.request.SignupRequest;
import com.maskting.backend.service.SmsService;
import com.maskting.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final SmsService smsService;

    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(String nickname) {
        return ResponseEntity.ok(userService.checkNickname(nickname));
    }

    @PostMapping("/sms")
    public ResponseEntity<?> sendSms(String phoneNumber) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException, URISyntaxException {
        smsService.sendSms(phoneNumber);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/check-sms")
    public ResponseEntity<?> checkSms(@RequestBody CheckSmsRequest checkSmsRequest) {
        return ResponseEntity.ok(smsService.checkVerificationNumber(checkSmsRequest));
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

    @GetMapping("/rejection")
    public ResponseEntity<?> getRejection(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return ResponseEntity.ok(userService.getRejection(user));
    }

    @GetMapping("/re-signup")
    public ResponseEntity<?> getReSignupInfo(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return ResponseEntity.ok(userService.getReSignupInfo(user));
    }

    @PostMapping("/re-signup")
    public ResponseEntity<?> reSignup(@Valid ReSignupRequest reSignupRequest, @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) throws IOException {
        userService.reSignup(user, reSignupRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/screening")
    public ResponseEntity<?> getScreeningResult(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return ResponseEntity.ok(userService.getScreeningResult(user));
    }
}
