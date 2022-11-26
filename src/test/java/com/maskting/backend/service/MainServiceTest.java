package com.maskting.backend.service;

import com.maskting.backend.common.exception.ExistLikeException;
import com.maskting.backend.domain.*;
import com.maskting.backend.dto.request.FeedRequest;
import com.maskting.backend.dto.response.PartnerResponse;
import com.maskting.backend.dto.response.S3Response;
import com.maskting.backend.dto.response.UserResponse;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.FeedRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.S3Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MainServiceTest {

    private UserFactory userFactory;
    private User user;
    private org.springframework.security.core.userdetails.User userDetail;

    @InjectMocks
    MainService mainService;

    @Mock
    S3Uploader s3Uploader;

    @Mock
    UserRepository userRepository;

    @Mock
    FeedRepository feedRepository;

    @Mock
    ChatRoomService chatRoomService;

    @Mock
    ChatUserService chatUserService;

    @Mock
    ChatService chatService;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        user = userFactory.createUser("테스트이름", "테스트닉네임");
        userDetail = new org.springframework.security.core.userdetails.User(user.getProviderId(), "", new ArrayList<>());
    }

    @Test
    @DisplayName("홈 유저 DTO 반환")
    void getUser() {
        given(userRepository.findByProviderId(any())).willReturn(user);

        UserResponse user = mainService.getUser(userDetail);

        assertTrue(user.getProfile().contains("amazon"));
        assertEquals("테스트닉네임", user.getNickname());
    }

    @Test
    @DisplayName("피드 추가")
    void addFeed() throws IOException {
        S3Response s3Response = new S3Response("feedName", "feedPath");
        given(userRepository.findByProviderId(any())).willReturn(user);
        given(s3Uploader.upload(any(), anyString())).willReturn(s3Response);
        given(feedRepository.save(any()))
                .willReturn(new Feed(1L, user, s3Response.getPath(), s3Response.getName()));
        given(feedRepository.count()).willReturn(1L);

        Feed feed = mainService.addFeed(userDetail,
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
        addInterests(user, new ArrayList<>(Arrays.asList("산책", "음악")));
        User partner1 = userFactory.getFemaleUserByInterests("test1", "공부", "게임");
        User partner2 = userFactory.getFemaleUserByInterests("test2", "산책", "게임");
        User partner3 = userFactory.getFemaleUserByInterests("test3", "산책", "음악");
        given(userRepository.findByProviderId(any())).willReturn(user);
        given(userRepository.findByLocationsAndGender(any(), anyString(), any()))
                .willReturn(new ArrayList<>(Arrays.asList(partner1, partner2, partner3)));

        List<User> partner = mainService.matchPartner(userDetail);

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

    @Test
    @DisplayName("좋아요 이미 있을 때 전송 한 경우 예외 처리")
    void sendLikeWithExistedLike() {
        User partner = sendLikeInit();
        user.addLike(partner);

        assertThatThrownBy(() -> mainService.sendLike(userDetail, partner.getNickname()))
                .isInstanceOf(ExistLikeException.class);
    }

    private User sendLikeInit() {
        User partner = userFactory.getFemaleUserByInterests("test1", "산책", "게임");
        given(userRepository.findByNickname(partner.getNickname())).willReturn(Optional.of(partner));
        given(userRepository.findByProviderId(any())).willReturn(user);
        return partner;
    }

    @Test
    @DisplayName("좋아요 전송")
    void sendLike() {
        User partner = sendLikeInit();
        assertTrue(user.getLikes().isEmpty());

        mainService.sendLike(userDetail, partner.getNickname());

        assertEquals(partner, user.getLikes().get(0));
    }

    @Test
    @DisplayName("상대가 나에게 좋아요가 있을 때 좋아요 전송시 매칭")
    void sendLikeWithMatching() {
        User partner = sendLikeInit();
        partner.addLike(user);
        ChatRoom chatRoom = new ChatRoom();
        ChatUser sendUser = getChatUser(chatRoom, user);
        ChatUser receiveUser = getChatUser(chatRoom, partner);
        given(chatRoomService.createRoom()).willReturn(chatRoom);
        given(chatUserService.createChatUser(user, chatRoom)).willReturn(sendUser);
        given(chatUserService.createChatUser(partner, chatRoom)).willReturn(receiveUser);

        mainService.sendLike(userDetail, partner.getNickname());

        assertTrue(chatRoom.getChatUsers().contains(sendUser));
        assertTrue(chatRoom.getChatUsers().contains(receiveUser));
    }

    private ChatUser getChatUser(ChatRoom chatRoom, User user) {
        return ChatUser.builder()
                .user(user)
                .chatRoom(chatRoom)
                .build();
    }
}