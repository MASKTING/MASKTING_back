package com.maskting.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maskting.backend.domain.ProviderType;
import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.SignupRequest;
import com.maskting.backend.repository.ProfileRepository;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.util.S3Uploader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.Cookie;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    S3Uploader s3Uploader;

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
        profileRepository.deleteAll();
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

    @Test
    @Transactional
    @DisplayName("추가 회원가입")
    void additionalSignup() throws Exception {
        User user = createUser();
        userRepository.save(user);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profiles", "test.jpg",
                "image/jpeg", new FileInputStream(new File("src/test/resources/test.jpg")));

        mockMvc.perform(
                multipart(pre + "/additional-signup")
                        .file(mockMultipartFile)
                        .param("providerId", "testProviderId")
                        .param("interest", "산책")
                        .param("duty", "true")
                        .param("smoking", "false")
                        .param("drinking", "5")
                        .param("religion", "무교")
                        .param("nickname", "테스트닉네임")
                        .with(requestPostProcessor -> {
                            requestPostProcessor.setMethod("POST");
                            return requestPostProcessor;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo(document("user/additional-signup",
                        requestParameters(
                                parameterWithName("providerId").description("제목")
                                ,parameterWithName("interest").description("취미")
                                ,parameterWithName("duty").description("군필")
                                ,parameterWithName("smoking").description("담배")
                                ,parameterWithName("drinking").description("음주")
                                ,parameterWithName("religion").description("종교")
                                ,parameterWithName("nickname").description("닉네임")
                        )
                        , requestParts(
                                partWithName("profiles").description("첨부 프로필")
                        )));

        User dbUser = userRepository.findByProviderId("testProviderId");
        assertEquals("산책", dbUser.getInterest());
        assertTrue(dbUser.isDuty());
        assertFalse(dbUser.isSmoking());
        assertEquals(5, dbUser.getDrinking());
        assertEquals("무교", dbUser.getReligion());
        assertEquals("테스트닉네임", dbUser.getNickname());
        assertTrue(dbUser.getProfiles().get(0).getName().contains("test.jpg"));
        s3Uploader.delete(dbUser.getProfiles().get(0).getName());
    }

    private User createUser() {
        User user = User.builder()
                .name("test")
                .email("test@gmail.com")
                .gender("male")
                .birth("19990815")
                .location("서울 강북구")
                .occupation("학생")
                .phone("01012345678")
                .roleType(RoleType.GUEST)
                .providerId("testProviderId")
                .providerType(ProviderType.GOOGLE)
                .profiles(new ArrayList<>())
                .build();
        return user;
    }


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