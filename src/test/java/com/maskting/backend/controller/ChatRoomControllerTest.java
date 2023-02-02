package com.maskting.backend.controller;

import com.maskting.backend.auth.WithAuthUser;
import com.maskting.backend.domain.*;
import com.maskting.backend.dto.request.ChatMessageRequest;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.*;
import com.maskting.backend.service.ChatService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
class ChatRoomControllerTest {

    private final String pre = "/chat";
    private UserFactory userFactory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatUserRepository chatUserRepository;

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FollowRepository followRepository;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
        userFactory = new UserFactory();
    }

    @AfterEach
    void tearDown() {
        chatMessageRepository.deleteAll();
        chatUserRepository.deleteAll();
        chatRoomRepository.deleteAll();
        followRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("전체 채팅방 조회")
    @WithAuthUser(id = "providerId_" + "jason", role = "ROLE_USER")
    void getAllChatRoom() throws Exception {
        User user = userRepository.save(userFactory.createUser("홍길동", "jason"));
        User partner1 = userRepository.save(userFactory.createUser("짱구", "gu"));
        User partner2 = userRepository.save(userFactory.createUser("철수", "su"));

        ChatRoom chatRoom1 = chatRoomRepository.save(
                ChatRoom
                .builder()
                .chatUsers(new ArrayList<>())
                .chatMessages(new ArrayList<>())
                .build()
        );
        chatService.saveChatMessage(new ChatMessageRequest(chatRoom1.getId(), user.getNickname(), "room1 마지막 메시지"));
        ChatRoom chatRoom2 = chatRoomRepository.save(
                ChatRoom
                .builder()
                .chatUsers(new ArrayList<>())
                .chatMessages(new ArrayList<>())
                .build()
        );
        chatService.saveChatMessage(new ChatMessageRequest(chatRoom2.getId(), user.getNickname(), "room2 마지막 메시지"));

        ChatUser chatUserInRoom1 = chatUserRepository.save(new ChatUser(10L, user, chatRoom1));
        ChatUser chatPartner1 = chatUserRepository.save(new ChatUser(20L, partner1, chatRoom1));
        ChatUser chatUserInRoom2 = chatUserRepository.save(new ChatUser(30L, user, chatRoom2));
        ChatUser chatPartner2 = chatUserRepository.save(new ChatUser(40L, partner2, chatRoom2));
        chatRoom1.addUser(chatUserInRoom1, chatPartner1);
        chatRoom2.addUser(chatUserInRoom2, chatPartner2);

        MvcResult mvcResult = mockMvc.perform(
                get(pre + "/rooms")
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andDo(document("chat/rooms"))
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains(chatRoom1.getId().toString()));
        assertTrue(mvcResult.getResponse().getContentAsString().contains(partner1.getNickname()));
        assertTrue(mvcResult.getResponse().getContentAsString().contains(chatRoom2.getId().toString()));
        assertTrue(mvcResult.getResponse().getContentAsString().contains(partner2.getNickname()));
        assertTrue(mvcResult.getResponse().getContentAsString().contains("\"update\":false"));
    }

    @Test
    @Transactional
    @DisplayName("특정 채팅방 조회")
    @WithAuthUser(id = "providerId_" + "jason", role = "ROLE_USER")
    void getChatRoom() throws Exception {
        User user = userRepository.save(userFactory.createUser("홍길동", "jason"));
        User partner = userRepository.save(userFactory.createUser("짱구", "gu"));

        ChatRoom chatRoom = chatRoomRepository.save(new ChatRoom(1L, new ArrayList<>(), new ArrayList<>()));
        chatService.saveChatMessage(new ChatMessageRequest(chatRoom.getId(), partner.getNickname(), "안녕"));
        chatService.saveChatMessage(new ChatMessageRequest(chatRoom.getId(), partner.getNickname(), "이름이 뭐야?"));

        ChatUser chatUser = chatUserRepository.save(new ChatUser(1L, user, chatRoom));
        ChatUser chatPartner = chatUserRepository.save(new ChatUser(2L, partner, chatRoom));
        chatRoom.addUser(chatUser, chatPartner);

        mockMvc.perform(
                get(pre + "/room/" + chatRoom.getId())
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(result -> result.getResponse().getContentAsString().contains("amazon"))
                .andExpect(result -> result.getResponse().getContentAsString().contains("gu"))
                .andExpect(result -> result.getResponse().getContentAsString().contains("안녕"))
                .andExpect(result -> result.getResponse().getContentAsString().contains("이름이 뭐야?"))
                .andDo(document("chat/room"));

        checkChatMessageChecked(user);
    }

    private void checkChatMessageChecked(User user) {
        List<ChatMessage> chatMessages = chatMessageRepository.findAll();
        for (ChatMessage chatMessage : chatMessages) {
            check(user, chatMessage);
        }
    }

    private void check(User user, ChatMessage chatMessage) {
        if (chatMessage.getUser().getId() != user.getId()) {
            assertTrue(chatMessage.isChecked());
        }
        if (chatMessage.getUser().getId() == user.getId()) {
            assertFalse(chatMessage.isChecked());
        }
    }

    @Test
    @Transactional
    @DisplayName("채팅방 나가기(상대 메시지 모두 check)")
    @WithAuthUser(id = "providerId_" + "jason", role = "ROLE_USER")
    void updateChatMessage() throws Exception {
        User user = userRepository.save(userFactory.createUser("홍길동", "jason"));
        User partner = userRepository.save(userFactory.createUser("짱구", "gu"));

        ChatRoom chatRoom = chatRoomRepository.save(new ChatRoom(1L, new ArrayList<>(), new ArrayList<>()));
        chatService.saveChatMessage(new ChatMessageRequest(chatRoom.getId(), partner.getNickname(), "안녕"));
        chatService.saveChatMessage(new ChatMessageRequest(chatRoom.getId(), partner.getNickname(), "이름이 뭐야?"));

        ChatUser chatUser = chatUserRepository.save(new ChatUser(1L, user, chatRoom));
        ChatUser chatPartner = chatUserRepository.save(new ChatUser(2L, partner, chatRoom));
        chatRoom.addUser(chatUser, chatPartner);

        mockMvc.perform(
                post(pre + "/room/" + chatRoom.getId() + "/out")
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andDo(document("chat/out"));

        checkPartnerMessage(user);
    }

    private void checkPartnerMessage(User user) {
        List<ChatMessage> chatMessages = chatMessageRepository.findAll();
        for (ChatMessage chatMessage : chatMessages) {
            if (chatMessage.getUser().getId() != user.getId()) {
                assertTrue(chatMessage.isChecked());
            }
        }
    }

    @Test
    @Transactional
    @DisplayName("팔로워들 반환")
    @WithAuthUser(id = "providerId_" + "jason", role = "ROLE_USER")
    void getFollowers() throws Exception {
        User user = userRepository.save(userFactory.createUser("홍길동", "jason"));
        User partner1 = userRepository.save(userFactory.createUser("짱구", "gu"));
        User partner2 = userRepository.save(userFactory.createUser("철수", "su"));
        saveFollow(user, partner1);
        saveFollow(user, partner2);

        MvcResult mvcResult = mockMvc.perform(
                get(pre + "/follower")
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andDo(document("chat/follower"))
                .andReturn();

        assertTrue(mvcResult.getResponse().getContentAsString().contains(partner1.getNickname()));
        assertTrue(mvcResult.getResponse().getContentAsString().contains(partner2.getNickname()));
    }

    private void saveFollow(User user, User partner1) {
        Follow follow = buildFollow(user, partner1);
        follow.updateUser(partner1, user);
        followRepository.save(follow);
    }

    private Follow buildFollow(User user, User partner) {
        return Follow.builder()
                .following(partner)
                .follower(user)
                .build();
    }

    @Test
    @Transactional
    @DisplayName("좋아요 거절")
    @WithAuthUser(id = "providerId_" + "jason", role = "ROLE_USER")
    void rejectFollower() throws Exception {
        User user = userRepository.save(userFactory.createUser("홍길동", "jason"));
        User partner1 = userRepository.save(userFactory.createUser("짱구", "gu"));
        User partner2 = userRepository.save(userFactory.createUser("철수", "su"));
        saveFollow(user, partner1);
        saveFollow(user, partner2);

        mockMvc.perform(
                post(pre + "/reject/" + partner1.getNickname())
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andDo(document("chat/reject"));

        assertEquals(1, followRepository.findAll().size());
    }

    @Test
    @Transactional
    @DisplayName("최종 프로필(본인) 사진 반환")
    @WithAuthUser(id = "providerId_" + "jason", role = "ROLE_USER")
    void getFinalProfiles() throws Exception {
        User user = userRepository.save(userFactory.createUser("홍길동", "jason"));

        mockMvc.perform(
                get(pre + "/profiles")
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("MASK")))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsString().contains("DEFAULT")))
                .andDo(document("chat/profiles"));
    }
}