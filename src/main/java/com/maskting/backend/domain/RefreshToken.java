package com.maskting.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "RefreshToken", timeToLive = 1209600000)
public class RefreshToken {

    @Id
    private String token;
    private String providerId;
}
