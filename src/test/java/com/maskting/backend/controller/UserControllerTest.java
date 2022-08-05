package com.maskting.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maskting.backend.domain.ProviderType;
import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.User;
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
    @Transactional
    @DisplayName("회원가입")
    void signup() throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("profiles", "test.jpg",
                "image/jpeg", new FileInputStream(new File("src/test/resources/test.jpg")));

        mockMvc.perform(
                multipart(pre + "/signup")
                        .file(mockMultipartFile)
                        .param("name", "test")
                        .param("email", "test@gmail.com")
                        .param("gender", "male")
                        .param("birth", "19990815")
                        .param("location", "서울 강북구")
                        .param("occupation", "학생")
                        .param("phone", "01012345678")
                        .param("providerId", "testProviderId")
                        .param("provider", "google")
                        .param("interest", "산책")
                        .param("duty", "true")
                        .param("smoking", "false")
                        .param("drinking", "5")
                        .param("height", "181")
                        .param("bodyType", "3")
                        .param("religion", "무교")
                        .param("nickname", "테스트닉네임")
                        .with(requestPostProcessor -> {
                            requestPostProcessor.setMethod("POST");
                            return requestPostProcessor;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(header().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andDo(document("user/signup",
                        requestParameters(
                                parameterWithName("name").description("이름")
                                ,parameterWithName("email").description("이메일")
                                ,parameterWithName("gender").description("성별")
                                ,parameterWithName("birth").description("생년월일")
                                ,parameterWithName("location").description("지역")
                                ,parameterWithName("occupation").description("직업")
                                ,parameterWithName("phone").description("전화번호")
                                ,parameterWithName("providerId").description("플랫폼 고유 id")
                                ,parameterWithName("provider").description("플랫폼 타입")
                                ,parameterWithName("interest").description("취미")
                                ,parameterWithName("duty").description("군필")
                                ,parameterWithName("smoking").description("담배")
                                ,parameterWithName("drinking").description("음주")
                                ,parameterWithName("height").description("키")
                                ,parameterWithName("bodyType").description("체형")
                                ,parameterWithName("religion").description("종교")
                                ,parameterWithName("nickname").description("닉네임")
                        )
                        , requestParts(
                                partWithName("profiles").description("첨부 프로필")
                        )));

        User dbUser = userRepository.findByProviderId("testProviderId");
        assertEquals("test", dbUser.getName());
        assertEquals("test@gmail.com", dbUser.getEmail());
        assertEquals("male", dbUser.getGender());
        assertEquals("19990815", dbUser.getBirth());
        assertEquals("서울 강북구", dbUser.getLocation());
        assertEquals("학생", dbUser.getOccupation());
        assertEquals("01012345678", dbUser.getPhone());
        assertEquals(ProviderType.GOOGLE, dbUser.getProviderType());
        assertEquals("산책", dbUser.getInterest());
        assertTrue(dbUser.isDuty());
        assertFalse(dbUser.isSmoking());
        assertEquals(5, dbUser.getDrinking());
        assertEquals(181, dbUser.getHeight());
        assertEquals(3, dbUser.getBodyType());
        assertEquals("무교", dbUser.getReligion());
        assertEquals("테스트닉네임", dbUser.getNickname());
        assertTrue(dbUser.getProfiles().get(0).getName().contains("test.jpg"));
        s3Uploader.delete(dbUser.getProfiles().get(0).getName());
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