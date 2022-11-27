package com.maskting.backend.controller;

import com.maskting.backend.config.StompFrameHandlerImpl;
import com.maskting.backend.domain.ChatRoom;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.ChatMessageRequest;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.ChatMessageRepository;
import com.maskting.backend.repository.ChatRoomRepository;
import com.maskting.backend.repository.ChatUserRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerTest {

    private UserFactory userFactory;
    private BlockingQueue<ChatMessageRequest> messages;

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatUserRepository chatUserRepository;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        messages = new LinkedBlockingDeque<>();
        userRepository.save(userFactory.createUser("홍길동", "jason"));
        chatRoomRepository.save(new ChatRoom(1L, new ArrayList<>(), new ArrayList<>()));
    }

    @AfterEach
    void tearDown() {
        chatMessageRepository.deleteAll();
        chatUserRepository.deleteAll();
        chatRoomRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("메시지 전송")
    void message() throws ExecutionException, InterruptedException, TimeoutException {
        ChatRoom chatRoom = chatRoomRepository.findAll().get(0);
        User user = userRepository.findAll().get(0);
        ChatMessageRequest expect = new ChatMessageRequest(chatRoom.getId(), user.getNickname(), "hello");

        //Setting
        WebSocketStompClient webSocketStompClient = WebSocketStompClient();
        webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

        //Connection
        HttpHeaders headers = new HttpHeaders(new LinkedMultiValueMap<>());
        headers.add("accessToken", jwtUtil.createAccessToken(user.getProviderId(), "ROLE_" + user.getRoleType().toString()));
        final WebSocketHttpHeaders socketHttpHeaders = new WebSocketHttpHeaders(headers);
        ListenableFuture<StompSession> connect = webSocketStompClient
                .connect("ws://localhost:" + port + "/app", socketHttpHeaders, new StompSessionHandlerAdapter() {
                });
        StompSession stompSession = connect.get(60, TimeUnit.SECONDS);

        stompSession.subscribe(String.format("/sub/chat/room/%s", chatRoom.getId()), new StompFrameHandlerImpl(new ChatMessageRequest(), messages));
        stompSession.send("/pub/chat/message", new ChatMessageRequest(1L, "jason", "hello"));

        ChatMessageRequest response = messages.poll(5, TimeUnit.SECONDS);

        //Then
        assertEquals(expect.getMessage(), response.getMessage());
    }

    private WebSocketStompClient WebSocketStompClient() {
        StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
        WebSocketTransport webSocketTransport = new WebSocketTransport(standardWebSocketClient);
        List<Transport> transports = Collections.singletonList(webSocketTransport);
        SockJsClient sockJsClient = new SockJsClient(transports);

        return new WebSocketStompClient(sockJsClient);
    }
}