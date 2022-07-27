package com.maskting.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.SignupRequest;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Cookie;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
class UserControllerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    CookieUtil cookieUtil;

    private final String pre = "/api/user";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("기본 회원가입")
    void signup() throws Exception {
        SignupRequest signupRequest = new SignupRequest(
                "test", "test@gmail.com", "male",
                "19990815", "서울 강북구", "학생",
                "01012345678", "12341234", "google");
        String content = objectMapper.writeValueAsString(signupRequest);

        mockMvc.perform(
                post(pre + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(header().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andDo(document("user/signup",
                        preprocessRequest(prettyPrint())));

        User user = userRepository.findByProviderId("12341234");
        assertEquals("test", user.getName());
        assertEquals("test@gmail.com", user.getEmail());
    }

    //TODO 추가회원가입 test

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        String key = UUID.randomUUID().toString();
        String refreshToken = jwtUtil.createRefreshToken(key);
        Cookie cookie = createCookie(refreshToken);
        RefreshToken dbRefreshToken = new RefreshToken(key, "testProviderId");
        refreshTokenRepository.save(dbRefreshToken);

        assertNotNull(refreshTokenRepository.findById(key).orElse(null));
        mockMvc.perform(
                post(pre + "/logout")
                        .cookie(cookie)
                        .header("accessToken", "testAccessToken"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andDo(document("user/logout",
                        preprocessRequest(prettyPrint())));

        assertNull(refreshTokenRepository.findById(key).orElse(null));
    }

    private Cookie createCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(10000);
        return cookie;
    }
}