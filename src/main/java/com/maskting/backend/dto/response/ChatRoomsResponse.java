package com.maskting.backend.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomsResponse {

    private String profile;

    private Long roomId;

    private String roomName;

    private int remainingTime;

    private String lastMessage;

    private String lastUpdatedAt;

    private boolean update;
}
