package com.maskting.backend.service;

import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CookieUtil cookieUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public RefreshToken getRefreshToken(HttpServletRequest request) {
        //TODO common exception 만들기
        Cookie cookie = cookieUtil.getCookie(request, "refreshToken").orElseThrow();
        String refreshTokenId = cookie.getValue();
        RefreshToken refreshToken = refreshTokenRepository.findById(refreshTokenId).orElseThrow();
        return refreshToken;
    }

    public void setAccessToken(HttpServletResponse response, User user) {
        String role;
        if (user.getRoleType() == RoleType.USER)
            role = "ROLE_USER";
        else
            role = "ROLE_ADMIN";
        String accessToken = jwtUtil.createAccessToken(user.getProviderId(), role);
        response.setHeader("accessToken", accessToken);
    }
}
