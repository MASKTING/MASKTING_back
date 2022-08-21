package com.maskting.backend.config.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maskting.backend.domain.User;
import com.maskting.backend.domain.oauth.UserPrincipal;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    private final String googleUserinfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo";
    private final String naverUserinfoUrl = "https://openapi.naver.com/v1/nid/me";
    private final String kakaoUserinfoUrl = "https://kapi.kakao.com/v2/user/me";
    private UserFactory userFactory;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HttpHeaders headers;
    private HttpEntity httpEntity;
    private ResponseEntity<String> responseEntity;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Mock
    RestTemplate restTemplate;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    CookieUtil cookieUtil;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        headers = new HttpHeaders();
        headers.add("Authorization","Bearer "+ "accessToken");
        httpEntity = new HttpEntity(headers);
        responseEntity = new ResponseEntity("", HttpStatus.OK);
    }

    @Test
    @DisplayName("가입이 안되어있을 때(첫 소셜로그인) - Google")
    void onAuthenticationSuccessWithFirstTimeByGoogle() throws ServletException, IOException {
        UserPrincipal userPrincipal = userFactory.createGoogleUser(null);
        Authentication authentication = userFactory.createAuthentication(userPrincipal);
        given(restTemplate.exchange(googleUserinfoUrl, HttpMethod.GET, httpEntity, String.class)).willReturn(responseEntity);
        JsonNode jsonNode = new ObjectMapper().readTree("{\"email\":\"test@gmail.com\"}");
        given(objectMapper.readTree(responseEntity.getBody())).willReturn(jsonNode);

        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        assertFirstResponse(userPrincipal);
    }

    private void assertFirstResponse(UserPrincipal userPrincipal) {
        assertEquals(302, response.getStatus());
        assertTrue(response.getRedirectedUrl().contains("provider=" + userPrincipal.getOAuth2UserInfo().getProvider()));
        assertTrue(response.getRedirectedUrl().contains("email=test@gmail.com"));
    }

    @Test
    @DisplayName("가입이 되어있지만 Guest일때 - Google")
    void onAuthenticationSuccessAsGuestByGoogle() throws ServletException, IOException {
        User guest = userFactory.createGuest("testName", "testNickname");
        UserPrincipal userPrincipal = userFactory.createGoogleUser(guest);
        Authentication authentication = userFactory.createAuthentication(userPrincipal);
        given(jwtUtil.createAccessToken(userPrincipal.getOAuth2UserInfo().getProviderId()
                , "ROLE_" + userPrincipal.getUser().getRoleType().toString()))
                .willReturn("accessToken");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("refreshToken");
        given(jwtUtil.getRefreshTokenValidTime()).willReturn(10000);

        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        assertGuestResponse();
        checkToken();
    }

    private void assertGuestResponse() {
        assertEquals(302, response.getStatus());
        assertFalse(response.getRedirectedUrl().contains("provider="));
        assertFalse(response.getRedirectedUrl().contains("email="));
        assertTrue(response.getRedirectedUrl().contains("sort="));
    }

    private void checkToken() {
        assertTrue(response.containsHeader("accessToken"));
        verify(cookieUtil).addCookie(response, "refreshToken", "refreshToken", 10000);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    @DisplayName("가입이 되어있고 User일때 - Google")
    void onAuthenticationSuccessAsUserByGoogle() throws ServletException, IOException {
        User user = userFactory.createUser("testName", "testNickname");
        UserPrincipal userPrincipal = userFactory.createGoogleUser(user);
        Authentication authentication = userFactory.createAuthentication(userPrincipal);
        given(jwtUtil.createAccessToken(userPrincipal.getOAuth2UserInfo().getProviderId()
                , "ROLE_" + userPrincipal.getUser().getRoleType().toString()))
                .willReturn("accessToken");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("refreshToken");
        given(jwtUtil.getRefreshTokenValidTime()).willReturn(10000);

        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        assertUserResponse();
        checkToken();
    }

    private void assertUserResponse() {
        assertEquals(302, response.getStatus());
        assertFalse(response.getRedirectedUrl().contains("provider="));
        assertFalse(response.getRedirectedUrl().contains("email="));
        assertFalse(response.getRedirectedUrl().contains("sort"));
        assertTrue(response.getRedirectedUrl().contains("user"));
    }

    @Test
    @DisplayName("가입이 안되어있을 때(첫 소셜로그인) - Naver")
    void onAuthenticationSuccessWithFirstTimeByNaver() throws ServletException, IOException {
        UserPrincipal userPrincipal = userFactory.createNaverUser(null);
        Authentication authentication = userFactory.createAuthentication(userPrincipal);
        given(restTemplate.exchange(naverUserinfoUrl, HttpMethod.GET, httpEntity, String.class)).willReturn(responseEntity);
        JsonNode jsonNode = new ObjectMapper().readTree("{\"response\":{\"email\":\"test@gmail.com\"}}");
        given(objectMapper.readTree(responseEntity.getBody())).willReturn(jsonNode);

        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        assertFirstResponse(userPrincipal);
    }

    @Test
    @DisplayName("가입이 되어있지만 Guest일때 - Naver")
    void onAuthenticationSuccessAsGuestByNaver() throws ServletException, IOException {
        User guest = userFactory.createGuest("testName", "testNickname");
        UserPrincipal userPrincipal = userFactory.createNaverUser(guest);
        Authentication authentication = userFactory.createAuthentication(userPrincipal);
        given(jwtUtil.createAccessToken(userPrincipal.getOAuth2UserInfo().getProviderId()
                , "ROLE_" + userPrincipal.getUser().getRoleType().toString()))
                .willReturn("accessToken");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("refreshToken");
        given(jwtUtil.getRefreshTokenValidTime()).willReturn(10000);

        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        assertGuestResponse();
        checkToken();
    }

    @Test
    @DisplayName("가입이 되어있고 User일때 - Naver")
    void onAuthenticationSuccessAsUserByNaver() throws ServletException, IOException {
        User user = userFactory.createUser("testName", "testNickname");
        UserPrincipal userPrincipal = userFactory.createNaverUser(user);
        Authentication authentication = userFactory.createAuthentication(userPrincipal);
        given(jwtUtil.createAccessToken(userPrincipal.getOAuth2UserInfo().getProviderId()
                , "ROLE_" + userPrincipal.getUser().getRoleType().toString()))
                .willReturn("accessToken");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("refreshToken");
        given(jwtUtil.getRefreshTokenValidTime()).willReturn(10000);

        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        assertUserResponse();
        checkToken();
    }

    @Test
    @DisplayName("가입이 안되어있을 때(첫 소셜로그인) - Kakao")
    void onAuthenticationSuccessWithFirstTimeByKakao() throws ServletException, IOException {
        UserPrincipal userPrincipal = userFactory.createKakaoUser(null);
        Authentication authentication = userFactory.createAuthentication(userPrincipal);
        given(restTemplate.exchange(kakaoUserinfoUrl, HttpMethod.GET, httpEntity, String.class)).willReturn(responseEntity);
        JsonNode jsonNode = new ObjectMapper().readTree("{\"kakao_account\":{\"email\":\"test@gmail.com\"}}");
        given(objectMapper.readTree(responseEntity.getBody())).willReturn(jsonNode);

        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        assertFirstResponse(userPrincipal);
    }

    @Test
    @DisplayName("가입이 되어있지만 Guest일때 - Kakao")
    void onAuthenticationSuccessAsGuestByKakao() throws ServletException, IOException {
        User guest = userFactory.createGuest("testName", "testNickname");
        UserPrincipal userPrincipal = userFactory.createKakaoUser(guest);
        Authentication authentication = userFactory.createAuthentication(userPrincipal);
        given(jwtUtil.createAccessToken(userPrincipal.getOAuth2UserInfo().getProviderId()
                , "ROLE_" + userPrincipal.getUser().getRoleType().toString()))
                .willReturn("accessToken");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("refreshToken");
        given(jwtUtil.getRefreshTokenValidTime()).willReturn(10000);

        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        assertGuestResponse();
        checkToken();
    }

    @Test
    @DisplayName("가입이 되어있고 User일때 - Kakao")
    void onAuthenticationSuccessAsUserByKakao() throws ServletException, IOException {
        User user = userFactory.createUser("testName", "testNickname");
        UserPrincipal userPrincipal = userFactory.createKakaoUser(user);
        Authentication authentication = userFactory.createAuthentication(userPrincipal);
        given(jwtUtil.createAccessToken(userPrincipal.getOAuth2UserInfo().getProviderId()
                , "ROLE_" + userPrincipal.getUser().getRoleType().toString()))
                .willReturn("accessToken");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("refreshToken");
        given(jwtUtil.getRefreshTokenValidTime()).willReturn(10000);

        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        assertUserResponse();
        checkToken();
    }
}