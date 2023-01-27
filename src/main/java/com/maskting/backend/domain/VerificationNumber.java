package com.maskting.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "VerificationNumber", timeToLive = 180000)
public class VerificationNumber {

    @Id
    private String id;
    private String value;
}
