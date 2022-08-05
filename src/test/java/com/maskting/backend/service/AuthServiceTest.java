package com.maskting.backend.service;

import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.User;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.common.exception.NoCookieException;
import com.maskting.backend.common.exception.NoRefreshTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("리프레쉬 토큰 반환 - 성공")
    void getRefreshTokenSuccess() {
        String key = "testKey";
        String providerId = "testProviderId";
        Cookie cookie = createCookie(key);
        given(cookieUtil.getCookie(any(HttpServletRequest.class), anyString())).willReturn(Optional.of(cookie));
        given(jwtUtil.getSubject(anyString())).willReturn(key);
        given(refreshTokenRepository.findById(key))
                .willReturn(Optional.of(new RefreshToken(key, providerId)));
        HttpServletRequest request = mock(HttpServletRequest.class);

        RefreshToken refreshToken = authService.getRefreshToken(request);

        assertEquals(key, refreshToken.getId());
        assertEquals(providerId, refreshToken.getProviderId());
    }

    private Cookie createCookie(String key) {
        Cookie cookie = new Cookie("refreshToken", key);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(100000);
        return cookie;
    }

    @Test
    @DisplayName("리프레쉬 토큰 반환 - 쿠키 없는 경우")
    void getRefreshTokenWhenNoCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThrows(NoCookieException.class, () ->{
            authService.getRefreshToken(request);
        });
    }

    @Test
    @DisplayName("리프레쉬 토큰 반환 - 리프레쉬 토큰 없는 경우")
    void getRefreshTokenWhenNoRefreshToken() {
        String key = "testKey";
        Cookie cookie = createCookie(key);
        given(cookieUtil.getCookie(any(HttpServletRequest.class), anyString())).willReturn(Optional.of(cookie));
        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThrows(NoRefreshTokenException.class, () ->{
            authService.getRefreshToken(request);
        });
    }

    @Test
    @DisplayName("헤더에 액세스 토큰 저장 - 성공")
    void setAccessToken() {
        UserFactory userFactory = new UserFactory();
        User user = userFactory.createUser();
        HttpServletResponse response = mock(HttpServletResponse.class);
        given(userRepository.findByProviderId("testProviderId")).willReturn(user);
        given(jwtUtil.createAccessToken(anyString(), anyString())).willReturn(any());
        given(response.getHeader("accessToken")).willReturn("testToken");

        authService.setAccessToken(response, new RefreshToken("testId", "testProviderId"));

        assertEquals("testToken", response.getHeader("accessToken"));
    }

    @Test
    @DisplayName("헤더에 액세스 토큰 저장 - 유저가 없는 경우")
    void setAccessTokenWhenNoUser() {
        HttpServletResponse response = mock(HttpServletResponse.class);

        assertThrows(UsernameNotFoundException.class, () ->{
            authService.setAccessToken(response, new RefreshToken("testId", "testProviderId"));
        });
    }
}