package com.maskting.backend.service;

import com.maskting.backend.domain.Feed;
import com.maskting.backend.domain.Interest;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.FeedRequest;
import com.maskting.backend.dto.response.PartnerResponse;
import com.maskting.backend.dto.response.S3Response;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.FeedRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.util.S3Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MainServiceTest {

    private UserFactory userFactory;

    @InjectMocks
    MainService mainService;

    @Mock
    S3Uploader s3Uploader;

    @Mock
    UserRepository userRepository;

    @Mock
    FeedRepository feedRepository;

    @Mock
    JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
    }

    @Test
    @DisplayName("피드 추가")
    void addFeed() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        User user = userFactory.createUser("피드테스트이름", "피드테스트닉네임");
        S3Response s3Response = new S3Response("feedName", "feedPath");
        given(userRepository.findByProviderId(any())).willReturn(user);
        given(s3Uploader.upload(any(), anyString())).willReturn(s3Response);
        given(feedRepository.save(any()))
                .willReturn(new Feed(1L, user, s3Response.getPath(), s3Response.getName()));
        given(feedRepository.count()).willReturn(1L);

        Feed feed = mainService.addFeed(request,
                new FeedRequest(new MockMultipartFile("test", "test.png",
                "image/png", "test data".getBytes())));

        assertEquals("feedName", feed.getName());
        assertEquals("feedPath", feed.getPath());
        assertEquals(1, user.getFeeds().size());
        assertEquals(1, feedRepository.count());
    }

    @Test
    @DisplayName("파트너 매칭")
    void matchPartner() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        User user = userFactory.createUser("매칭테스트이름", "매칭테스트닉네임");
        addInterests(user, new ArrayList<>(Arrays.asList("산책", "음악")));
        User partner1 = userFactory.getFemaleUserByInterests("test1", "공부", "게임");
        User partner2 = userFactory.getFemaleUserByInterests("test2", "산책", "게임");
        User partner3 = userFactory.getFemaleUserByInterests("test3", "산책", "음악");
        given(userRepository.findByProviderId(any())).willReturn(user);
        given(userRepository.findByLocationsAndGender(any(), anyString(), any()))
                .willReturn(new ArrayList<>(Arrays.asList(partner1, partner2, partner3)));

        List<User> partner = mainService.matchPartner(request);

        assertEquals(partner3, partner.get(0));
        assertEquals(partner2, partner.get(1));
    }

    private void addInterests(User user, List<String> names) {
        List<Interest> interests = new ArrayList<>();
        for (String name : names) {
            interests.add(getInterest((name)));
        }
        user.addInterests(interests);
    }

    private Interest getInterest(String name) {
        return Interest.builder()
                .name(name)
                .build();
    }

    @Test
    @DisplayName("파트너 DTO 반환")
    void getPartnerResponse() {
        User partner1 = userFactory.getFemaleUserByInterests("test1", "산책", "게임");
        addFeed(partner1);
        User partner2 = userFactory.getFemaleUserByInterests("test2", "산책", "음악");

        List<PartnerResponse> partnerResponse = mainService
                .getPartnerResponse(new ArrayList<>(Arrays.asList(partner1, partner2)));

        assertEquals(2, partnerResponse.size());
        assertEquals(1, partnerResponse.get(0).getFeed().size());
    }

    private void addFeed(User partner1) {
        Feed build = Feed.builder()
                .path("test")
                .name("test")
                .build();
        build.updateUser(partner1);
    }

}