package com.maskting.backend.service;

import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.common.exception.NoCookieException;
import com.maskting.backend.common.exception.NoRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final UserRepository userRepository;

    public RefreshToken getRefreshToken(HttpServletRequest request) {
        Cookie cookie = cookieUtil.getCookie(request, "refreshToken").orElseThrow(NoCookieException::new);
        String refreshTokenId = cookie.getValue();
        //TODO resolve 필요
        RefreshToken refreshToken = refreshTokenRepository
                .findById(refreshTokenId)
                .orElseThrow(NoRefreshTokenException::new);

        return refreshToken;
    }

    public void setAccessToken(HttpServletResponse response, RefreshToken refreshToken) {
        User user = userRepository.findByProviderId(refreshToken.getProviderId());
        if (user == null) {
            throw new UsernameNotFoundException("유저가 존재하지 않습니다.");
        }

        String role;
        if (user.getRoleType() == RoleType.USER)
            role = "ROLE_USER";
        else
            role = "ROLE_ADMIN";

        String accessToken = jwtUtil.createAccessToken(user.getProviderId(), role);
        response.setHeader("accessToken", accessToken);
    }
}
