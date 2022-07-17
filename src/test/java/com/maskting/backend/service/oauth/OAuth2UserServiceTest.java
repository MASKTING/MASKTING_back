package com.maskting.backend.service.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class OAuth2UserServiceTest {

    @Test
    @DisplayName("OAuth2 인증 반환")
    public void loadUser() {
        OAuth2UserService oAuth2UserService = mock(OAuth2UserService.class);
        OAuth2User oAuth2User = mock(OAuth2User.class);
        given(oAuth2UserService.loadUser(any(OAuth2UserRequest.class))).willReturn(oAuth2User);

        OAuth2User loadedUser = oAuth2UserService.loadUser(mock((OAuth2UserRequest.class)));

        assertEquals(oAuth2User, loadedUser);
    }

    @Test
    @DisplayName("UserRequest가 Null일때")
    public void loadUserWhenUserRequestIsNull() {
        OAuth2UserService oAuth2UserService = mock(OAuth2UserService.class);
        given(oAuth2UserService.loadUser(null)).willThrow(IllegalArgumentException.class);

        assertThatIllegalArgumentException().isThrownBy(() -> oAuth2UserService.loadUser(null));
    }

    @Test
    @DisplayName("UserService가 로드되지 않은 경우")
    public void loadUserWhenUserServiceCannotLoad() {
        OAuth2UserService oAuth2UserService = mock(OAuth2UserService.class);

        OAuth2User loadedUser = oAuth2UserService.loadUser(mock((OAuth2UserRequest.class)));

        assertNull(loadedUser);
    }
}