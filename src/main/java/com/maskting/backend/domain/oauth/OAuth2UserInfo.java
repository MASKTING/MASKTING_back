package com.maskting.backend.domain.oauth;

import java.util.Map;

public interface OAuth2UserInfo {
    Map<String, Object> getAttributes();
    String getProvider();
    String getProviderId();
}
