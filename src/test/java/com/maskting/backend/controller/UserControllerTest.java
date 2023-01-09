package com.maskting.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maskting.backend.auth.WithAuthUser;
import com.maskting.backend.domain.ProviderType;
import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.SignupRequest;
import com.maskting.backend.factory.RequestFactory;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.ProfileRepository;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.util.S3MockConfig;
import com.maskting.backend.util.S3Uploader;
import io.findify.s3mock.S3Mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(S3MockConfig.class)
@ExtendWith(RestDocumentationExtension.class)
class UserControllerTest {

    private static final int DEFAULT_PROFILE = 0;
    private static final int MASK_PROFILE = 1;
    private static final String pre = "/api/user";
    private RequestFactory requestFactory;
    private UserFactory userFactory;

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    S3Mock s3Mock;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(documentationConfiguration(restDocumentation))
                .build();
        requestFactory = new RequestFactory();
        userFactory = new UserFactory();
    }

    @AfterEach
    void tearDown() {
        profileRepository.deleteAll();
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        s3Mock.stop();
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 가능")
    void checkNicknameWithSuccess() throws Exception {
        String nickname = "test";
        String result = "true";

        MvcResult mvcResult = mockMvc.perform(
                get(pre + "/check-nickname")
                        .param("nickname", nickname)
                        .header("accessToken", "testAccessToken"))
                .andExpect(status().isOk())
                .andDo(document("user/check-nickname-success",
                        preprocessRequest(prettyPrint())))
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains(result));
    }

    @Test
    @DisplayName("닉네임 중복 확인 - 불가능")
    void checkNicknameWithFail() throws Exception {
        String nickname = "test";
        String result = "false";
        userRepository.save(userFactory.createUser("이름", nickname));

        MvcResult mvcResult = mockMvc.perform(
                get(pre + "/check-nickname")
                        .param("nickname", nickname)
                        .header("accessToken", "testAccessToken"))
                .andExpect(status().isOk())
                .andDo(document("user/check-nickname-fail",
                        preprocessRequest(prettyPrint())))
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains(result));
    }

    @Test
    @Transactional
    @DisplayName("회원가입")
    void signup() throws Exception {
        SignupRequest signupRequest = requestFactory.createSignupRequest();
        MultiValueMap<String, String> interests = new LinkedMultiValueMap<>();
        interests.add("interests", signupRequest.getInterests().get(0));
        MultiValueMap<String, String> partnerLocations = new LinkedMultiValueMap<>();
        partnerLocations.add("partnerLocations", signupRequest.getPartnerLocations().get(0));
        MultiValueMap<String, String> partnerReligions = new LinkedMultiValueMap<>();
        partnerReligions.add("partnerReligions", signupRequest.getPartnerReligions().get(0));
        MultiValueMap<String, String> partnerBodyTypes = new LinkedMultiValueMap<>();
        partnerBodyTypes.add("partnerBodyTypes", String.valueOf(signupRequest.getPartnerBodyTypes().get(0)));

        mockMvc.perform(
                multipart(pre + "/signup")
                        .file((MockMultipartFile) signupRequest.getProfiles().get(DEFAULT_PROFILE))
                        .file((MockMultipartFile) signupRequest.getProfiles().get(MASK_PROFILE))
                        .param("name", signupRequest.getName())
                        .param("email", signupRequest.getEmail())
                        .param("gender", signupRequest.getGender())
                        .param("birth", signupRequest.getBirth())
                        .param("location", signupRequest.getLocation())
                        .param("occupation", signupRequest.getOccupation())
                        .param("phone", signupRequest.getPhone())
                        .param("providerId", signupRequest.getProviderId())
                        .param("provider", signupRequest.getProvider())
                        .params(interests)
                        .param("duty", String.valueOf(signupRequest.isDuty()))
                        .param("smoking", String.valueOf(signupRequest.isSmoking()))
                        .param("drinking", Integer.toString(signupRequest.getDrinking()))
                        .param("height", Integer.toString(signupRequest.getHeight()))
                        .param("bodyType", Integer.toString(signupRequest.getBodyType()))
                        .param("religion", signupRequest.getReligion())
                        .param("bio", signupRequest.getBio())
                        .param("nickname", signupRequest.getNickname())
                        .params(partnerLocations)
                        .param("partnerDuty", signupRequest.getPartnerDuty())
                        .param("partnerSmoking", signupRequest.getPartnerSmoking())
                        .params(partnerReligions)
                        .param("partnerDrinking", Integer.toString(signupRequest.getPartnerDrinking()))
                        .param("partnerMinHeight", Integer.toString(signupRequest.getPartnerMinHeight()))
                        .param("partnerMaxHeight", Integer.toString(signupRequest.getPartnerMaxHeight()))
                        .params(partnerBodyTypes)
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
                                ,parameterWithName("interests").description("취미(List)")
                                ,parameterWithName("duty").description("군필")
                                ,parameterWithName("smoking").description("담배")
                                ,parameterWithName("drinking").description("음주")
                                ,parameterWithName("height").description("키")
                                ,parameterWithName("bodyType").description("체형")
                                ,parameterWithName("religion").description("종교")
                                ,parameterWithName("bio").description("한줄 자기소개")
                                ,parameterWithName("nickname").description("닉네임")
                                ,parameterWithName("partnerLocations").description("상대방 선호 지역(List)")
                                ,parameterWithName("partnerDuty").description("상대방 군필여부(여자인경우만, 남자인 경우 any)")
                                ,parameterWithName("partnerSmoking").description("상대방 흡연 여부(상관없는 경우 any)")
                                ,parameterWithName("partnerReligions").description("상대방 선호 종교(List)")
                                ,parameterWithName("partnerDrinking").description("상대방 음주")
                                ,parameterWithName("partnerMinHeight").description("상대방 선호 최소키")
                                ,parameterWithName("partnerMaxHeight").description("상대방 선호 최대키")
                                ,parameterWithName("partnerBodyTypes").description("상대방 선호 체형(List)")
                        )
                        , requestParts(
                                partWithName("profiles").description("첨부 프로필(기본 프로필, 마스크 프로필)")
                        )));

        User dbUser = userRepository.findByProviderId(signupRequest.getProviderId());
        assertUserInfo(signupRequest, dbUser);
        assertPartnerInfo(signupRequest, dbUser);
    }

    private void assertPartnerInfo(SignupRequest signupRequest, User dbUser) {
        assertEquals(signupRequest.getPartnerLocations().get(0), dbUser.getPartnerLocations().get(0).getName());
        assertEquals(signupRequest.getPartnerDuty(), dbUser.getPartner().getPartnerDuty());
        assertEquals(signupRequest.getPartnerSmoking(), dbUser.getPartner().getPartnerSmoking());
        assertEquals(signupRequest.getPartnerReligions().get(0), dbUser.getPartnerReligions().get(0).getName());
        assertEquals(signupRequest.getPartnerDrinking(), dbUser.getPartner().getPartnerDrinking());
        assertEquals(signupRequest.getPartnerMinHeight(), dbUser.getPartner().getPartnerMinHeight());
        assertEquals(signupRequest.getPartnerMaxHeight(), dbUser.getPartner().getPartnerMaxHeight());
        assertEquals(signupRequest.getPartnerBodyTypes().get(0), dbUser.getPartnerBodyTypes().get(0).getVal());
    }

    private void assertUserInfo(SignupRequest signupRequest, User dbUser) {
        assertEquals(signupRequest.getName(), dbUser.getName());
        assertEquals(signupRequest.getEmail(), dbUser.getEmail());
        assertEquals(signupRequest.getGender(), dbUser.getGender());
        assertEquals(signupRequest.getBirth(), dbUser.getBirth());
        assertEquals(signupRequest.getLocation(), dbUser.getLocation());
        assertEquals(signupRequest.getOccupation(), dbUser.getOccupation());
        assertEquals(signupRequest.getPhone(), dbUser.getPhone());
        assertEquals(ProviderType.GOOGLE, dbUser.getProviderType());
        assertEquals(signupRequest.getInterests().get(0), dbUser.getInterests().get(0).getName());
        assertTrue(dbUser.isDuty());
        assertFalse(dbUser.isSmoking());
        assertEquals(signupRequest.getDrinking(), dbUser.getDrinking());
        assertEquals(signupRequest.getHeight(), dbUser.getHeight());
        assertEquals(signupRequest.getBodyType(), dbUser.getBodyType());
        assertEquals(signupRequest.getReligion(), dbUser.getReligion());
        assertEquals(signupRequest.getBio(), dbUser.getBio());
        assertEquals(signupRequest.getNickname(), dbUser.getNickname());
        assertTrue(dbUser.getProfiles().get(DEFAULT_PROFILE).getName().contains("DEFAULT_IMAGE.PNG"));
        assertTrue(dbUser.getProfiles().get(MASK_PROFILE).getName().contains("MASK_IMAGE.PNG"));
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

    @Test
    @Transactional
    @DisplayName("반려 사유 반환")
    @WithAuthUser(id = "providerId_" + "test", role = "ROLE_GUEST")
    void getRejection() throws Exception {
        String reason = "부적절한 프로필입니다.";
        User guest = userFactory.createGuest("test", "test");
        userRepository.save(guest);
        guest.updateRejection(reason);

        MvcResult mvcResult = mockMvc.perform(
                get(pre + "/rejection")
                        .header("accessToken", jwtUtil.createAccessToken(guest.getProviderId(), "ROLE_GUEST")))
                .andExpect(status().isOk())
                .andDo(document("user/rejection"))
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains(reason));
    }

    @Test
    @Transactional
    @DisplayName("수정 정보 반환")
    @WithAuthUser(id = "providerId_" + "test", role = "ROLE_GUEST")
    void getReSignupInfo() throws Exception {
        User guest = userFactory.createGuest("test", "test");
        userRepository.save(guest);

        MvcResult mvcResult = mockMvc.perform(
                get(pre + "/re-signup")
                        .header("accessToken", jwtUtil.createAccessToken(guest.getProviderId(), "ROLE_GUEST")))
                .andExpect(status().isOk())
                .andDo(document("user/re-signup"))
                .andReturn();

        checkReSignupInfo(guest, mvcResult);
    }

    private void checkReSignupInfo(User guest, MvcResult mvcResult) throws UnsupportedEncodingException {
        assertTrue(mvcResult.getResponse().getContentAsString().contains(guest.getName()));
        assertTrue(mvcResult.getResponse().getContentAsString().contains(guest.getBirth()));
        assertTrue(mvcResult.getResponse().getContentAsString().contains(String.valueOf(guest.getHeight())));
        assertTrue(mvcResult.getResponse().getContentAsString().contains(guest.getNickname()));
        assertTrue(mvcResult.getResponse().getContentAsString().contains(guest.getBio()));
        assertTrue(mvcResult.getResponse().getContentAsString().contains(guest.getProfiles().get(DEFAULT_PROFILE).getPath()));
        assertTrue(mvcResult.getResponse().getContentAsString().contains(guest.getProfiles().get(MASK_PROFILE).getPath()));
    }
}