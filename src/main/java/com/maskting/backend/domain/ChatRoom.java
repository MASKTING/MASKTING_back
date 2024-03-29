package com.maskting.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "chatRoom")
    private List<ChatUser> chatUsers = new ArrayList<>();

    @OneToMany(mappedBy = "chatRoom")
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ChatRoomResult result;

    public void addUser(ChatUser sendUser, ChatUser receiveUser) {
        chatUsers.add(sendUser);
        chatUsers.add(receiveUser);
    }

    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
    }

    public void updateResult(ChatRoomResult result) {
        this.result = result;
    }
}
