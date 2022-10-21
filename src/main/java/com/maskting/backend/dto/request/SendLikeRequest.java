package com.maskting.backend.dto.request;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class SendLikeRequest {

    @NotNull
    private String nickname;
}
