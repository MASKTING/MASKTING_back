package com.maskting.backend.controller;

import com.maskting.backend.auth.WithAuthUser;
import com.maskting.backend.domain.ChatRoom;
import com.maskting.backend.domain.ChatUser;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.ChatMessageRequest;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.ChatMessageRepository;
import com.maskting.backend.repository.ChatRoomRepository;
import com.maskting.backend.repository.ChatUserRepository;
import com.maskting.backend.repository.UserRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.util.ArrayList;

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

        ChatUser chatUserInRoom1 = chatUserRepository.save(new ChatUser(1L, user, chatRoom1));
        ChatUser chatPartner1 = chatUserRepository.save(new ChatUser(2L, partner1, chatRoom1));
        ChatUser chatUserInRoom2 = chatUserRepository.save(new ChatUser(3L, user, chatRoom2));
        ChatUser chatPartner2 = chatUserRepository.save(new ChatUser(4L, partner2, chatRoom2));
        chatRoom1.addUser(chatUserInRoom1, chatPartner1);
        chatRoom2.addUser(chatUserInRoom2, chatPartner2);

        mockMvc.perform(
                get(pre + "/rooms")
                        .header("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(result -> result.getResponse().getContentAsString().contains(chatRoom1.getId().toString()))
                .andExpect(result -> result.getResponse().getContentAsString().contains(partner1.getNickname()))
                .andExpect(result -> result.getResponse().getContentAsString().contains(chatRoom2.getId().toString()))
                .andExpect(result -> result.getResponse().getContentAsString().contains(partner2.getNickname()))
                .andDo(document("chat/rooms"));
    }

    @Test
    @Transactional
    @DisplayName("특정 채팅방 조회")
    @WithAuthUser(id = "providerId_" + "jason", role = "ROLE_USER")
    void getChatRoom() throws Exception {
        User user = userRepository.save(userFactory.createUser("홍길동", "jason"));
        User partner = userRepository.save(userFactory.createUser("짱구", "gu"));

        ChatRoom chatRoom = chatRoomRepository.save(new ChatRoom(1L, new ArrayList<>(), new ArrayList<>()));
        chatService.saveChatMessage(new ChatMessageRequest(1L, partner.getNickname(), "안녕"));
        chatService.saveChatMessage(new ChatMessageRequest(1L, partner.getNickname(), "이름이 뭐야?"));

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
    }
}