package com.maskting.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {

    private String type;
    private String from;
    private String content;

    private List<MessageRequest> messages;
}
