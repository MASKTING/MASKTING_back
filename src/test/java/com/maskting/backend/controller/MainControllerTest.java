package com.maskting.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maskting.backend.auth.WithAuthUser;
import com.maskting.backend.domain.Feed;
import com.maskting.backend.domain.PartnerLocation;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.SendLikeRequest;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.FeedRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.util.S3MockConfig;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(S3MockConfig.class)
@ExtendWith(RestDocumentationExtension.class)
class MainControllerTest {

    private final String pre = "/api";
    private UserFactory userFactory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private S3Mock s3Mock;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

        userFactory = new UserFactory();
    }

    @AfterEach
    void tearDown() {
        s3Mock.stop();
        feedRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("홈 유저 반환")
    @WithAuthUser(id = "providerId_" + "test", role = "ROLE_USER")
    void getUser() throws Exception {
        User user = userFactory.createUser("test", "test");
        userRepository.save(user);

        mockMvc.perform(
                get(pre + "/user")
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(result -> result.getResponse().getContentAsString().contains("닉네임"))
                .andExpect(result -> result.getResponse().getContentAsString().contains("amazon"))
                .andDo(document("main/user"));
    }

    @Test
    @Transactional
    @DisplayName("피드 얻기")
    @WithAuthUser(id = "providerId_" + "test", role = "ROLE_USER")
    void getFeed() throws Exception {
        User user = userFactory.createUser("test", "test");
        userRepository.save(user);
        addFeed(user, "test1Path", "test1");
        addFeed(user, "test2Path", "test2");

        mockMvc.perform(
                get(pre + "/feed")
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(result -> result.getResponse().getContentAsString().contains("자기소개"))
                .andExpect(result -> result.getResponse().getContentAsString().contains("test1Path"))
                .andExpect(result -> result.getResponse().getContentAsString().contains("test2Path"))
                .andDo(document("main/get-feed"));
    }

    private void addFeed(User user, String path, String name) {
        Feed feed = Feed.builder()
                .name(name)
                .path(path)
                .build();
        feed.updateUser(user);
        feedRepository.save(feed);
    }

    @Test
    @Transactional
    @DisplayName("피드 추가")
    @WithAuthUser(id = "providerId_" + "test", role = "ROLE_USER")
    void addFeed() throws Exception {
        User user = userFactory.createUser("test", "test");
        userRepository.save(user);

        mockMvc.perform(
                multipart(pre + "/feed")
                        .file(new MockMultipartFile("feed",
                                "test.PNG", MediaType.IMAGE_PNG_VALUE, "test".getBytes()))
                        .with(requestPostProcessor -> {
                            requestPostProcessor.setMethod("POST");
                            return requestPostProcessor;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andDo(document("main/feed",
                        requestParts(partWithName("feed").description("피드"))));

        assertEquals(1, user.getFeeds().size());
        assertEquals(1, feedRepository.count());
    }

    @Test
    @Transactional
    @DisplayName("파트너 매칭")
    @WithAuthUser(id = "providerId_" + "test", role = "ROLE_USER")
    void getPartner() throws Exception {
        User user = createUser();
        getPartner("test1", "공부", "게임");
        User partner2 = getPartner("test2", "산책", "게임");
        User partner3 = getPartner("test3", "산책", "음악");

        mockMvc.perform(
                get(pre + "/partner")
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andDo(document("main/partner"));

        assertEquals(partner3, user.getMatches().get(0));
        assertEquals(partner2, user.getMatches().get(1));
    }

    private User getPartner(String nickname, String interest1, String interest2) {
        User partner = userFactory.getFemaleUserByInterests(nickname, interest1, interest2);
        userRepository.save(partner);
        return partner;
    }

    private User createUser() {
        User user = userFactory.createUser("test", "test");
        userFactory.addInterests(user, new ArrayList<>(Arrays.asList("산책", "음악")));
        PartnerLocation partnerLocation = PartnerLocation.builder()
                .name("경기 북부")
                .build();
        partnerLocation.updateUser(user);
        user.addPartnerLocations(new ArrayList<>(Arrays.asList(partnerLocation)));
        userRepository.save(user);
        return user;
    }

    @Test
    @Transactional
    @DisplayName("좋아요 전송")
    @WithAuthUser(id = "providerId_" + "test", role = "ROLE_USER")
    void sendLike() throws Exception {
        User user = createUser();
        User partner = getPartner("test1", "공부", "게임");
        String content = objectMapper.writeValueAsString(new SendLikeRequest(partner.getNickname()));

        mockMvc.perform(
                post(pre + "/like")
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER"))
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("main/like"));

        assertTrue(user.getLikes().contains(partner));
    }
}