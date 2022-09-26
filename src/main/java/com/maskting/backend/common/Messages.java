package com.maskting.backend.common;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Messages {
    public static final String INVALID_TOKEN = "유효하지 않은 토큰입니다.";
    public static final String MISS_MATCH_PROVIDER = "플랫폼이 일치하지 않습니다.";
    public static final String NO_REFRESH_TOKEN = "유효하지 않은 리프레쉬 토큰입니다.";
    public static final String NO_COOKIE = "유효하지 않은 쿠키입니다.";
    public static final String INVALID_PROVIDER = "유효하지 않은 플랫폼입니다.";
    public static final String NO_PROFILE = "등록한 프로필 사진이 없습니다.";
    public static final String NO_FEED = "등록한 피드 사진이 없습니다.";
    public static final String EXCEED_FEED_LIMIT = "피드 사진이 이미 6장입니다.";
}
