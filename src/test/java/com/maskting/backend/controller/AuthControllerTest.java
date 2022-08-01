package com.maskting.backend.controller;

import com.maskting.backend.domain.ProviderType;
import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
class AuthControllerTest {

    private final String pre = "/api/auth";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("RefreshToken 재발급")
    void silentRefresh() throws Exception {
        User user = createUser();
        userRepository.save(user);
        String key = UUID.randomUUID().toString();
        String refreshToken = jwtUtil.createRefreshToken(key);
        Cookie cookie = createCookie(refreshToken);
        RefreshToken dbRefreshToken = new RefreshToken(key, "testProviderId");
        refreshTokenRepository.save(dbRefreshToken);

        mockMvc.perform(
                post(pre + "/silent-refresh")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(header().exists("accessToken"))
                .andDo(document("auth/silent-refresh",
                        preprocessRequest(prettyPrint())));
    }

    private User createUser() {
        User user = User.builder()
                .name("test")
                .email("test@gmail.com")
                .gender("male")
                .birth("19990815")
                .location("서울 강북구")
                .occupation("대학생")
                .phone("01012345678")
                .roleType(RoleType.USER)
                .providerId("testProviderId")
                .providerType(ProviderType.GOOGLE)
                .build();
        return user;
    }

    private Cookie createCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(10000);
        return cookie;
    }
}