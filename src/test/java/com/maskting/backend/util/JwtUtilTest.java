package com.maskting.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private final String secretKey = Base64.getEncoder().encodeToString("'asdSAD2asTest2dSD@aa2Fa'".getBytes());
    private final long accessTokenValidTime = 100000;
    private final long refreshTokenValidTime = 500000;
    private static final String AUTHORITIES_KEY = "role";
    private final String testProviderId = "test1234";
    private final String role = "ROLE_USER";

    private JwtUtil jwtUtil;

    @BeforeEach
    void before() {
        jwtUtil = new JwtUtil(secretKey, accessTokenValidTime, refreshTokenValidTime);
    }

    @Test
    @DisplayName("액세스 토큰 생성")
    void createAccessToken() {
        String accessToken = jwtUtil.createAccessToken(testProviderId, role);
        String providerId = decodeJwt(accessToken).getSubject();
        Date issuedAt = decodeJwt(accessToken).getIssuedAt();
        Date expiration = decodeJwt(accessToken).getExpiration();
        String getRole = (String) decodeJwt(accessToken).get("role");

        assertEquals(testProviderId, providerId);
        assertEquals(issuedAt.getTime(), expiration.getTime() - accessTokenValidTime);
        assertEquals(role, getRole);
    }

    private Claims decodeJwt(String accessToken) {
        return jwtUtil.getClaimsJws(accessToken).getBody();
    }


    @Test
    @DisplayName("리프레쉬 토큰 생성")
    void createRefreshToken() {
        String testKey = "testKey";

        String accessToken = jwtUtil.createRefreshToken(testKey);
        String getKey = decodeJwt(accessToken).getSubject();
        Date issuedAt = decodeJwt(accessToken).getIssuedAt();
        Date expiration = decodeJwt(accessToken).getExpiration();

        assertEquals(testKey, getKey);
        assertEquals(issuedAt.getTime(), expiration.getTime() - refreshTokenValidTime);
    }

    @Test
    @DisplayName("인증 반환")
    void getAuthentication() {
        String accessToken = jwtUtil.createAccessToken(testProviderId, role);

        UsernamePasswordAuthenticationToken authentication
                = (UsernamePasswordAuthenticationToken) jwtUtil.getAuthentication(accessToken);
        User user = (User) authentication.getPrincipal();

        assertEquals(testProviderId, user.getUsername());
        assertEquals(user.getAuthorities().toString(), "[" + role + "]");

    }

    @Test
    @DisplayName("토큰으로부터 Subject(providerId)얻기")
    void getSubject() {
        String accessToken = jwtUtil.createAccessToken(testProviderId, role);

        String providerId = jwtUtil.getSubject(accessToken);

        assertEquals(testProviderId, providerId);
    }

    @Test
    @DisplayName("토큰 시간이 만료되었는지")
    void isTokenExpired() {
        String accessToken = jwtUtil.createAccessToken(testProviderId, role);

        Boolean tokenExpired = jwtUtil.isTokenExpired(accessToken);

        assertEquals(true, tokenExpired);
    }

    @Test
    @DisplayName("토큰이 유효한지")
    void validateToken() {
        String accessToken = jwtUtil.createAccessToken(testProviderId, role);

        Boolean tokenExpired = jwtUtil.validateToken(accessToken);

        assertEquals(true, tokenExpired);
    }

    @Test
    @DisplayName("헤더에서 액세스 토큰 반환")
    void resolveToken() {
        String accessToken = jwtUtil.createAccessToken(testProviderId, role);
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(request.getHeader("accessToken")).willReturn(accessToken);

        String resolveToken = jwtUtil.resolveToken(request);

        assertEquals(accessToken, resolveToken);
    }
}