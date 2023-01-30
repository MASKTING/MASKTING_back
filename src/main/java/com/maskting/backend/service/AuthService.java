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
        return refreshTokenRepository
                .findById(getKeyFromToken(request))
                .orElseThrow(NoRefreshTokenException::new);
    }

    private String getKeyFromToken(HttpServletRequest request) {
        return jwtUtil.getSubject(getCookie(request));
    }

    private String getCookie(HttpServletRequest request) {
        return cookieUtil.getCookie(request, "refreshToken").orElseThrow(NoCookieException::new).getValue();
    }

    public void setAccessToken(HttpServletResponse response, RefreshToken refreshToken) {
        User user = getUserByProviderId(refreshToken);
        if (user == null)
            throw new UsernameNotFoundException("유저가 존재하지 않습니다.");
        response.setHeader("accessToken", createAccessToken(user, getRole(user)));
    }

    private String getRole(User user) {
        if (user.getRoleType() == RoleType.GUEST) {
            return "ROLE_GUEST";
        }
        return user.getRoleType() == RoleType.USER ? "ROLE_USER" : "ROLE_ADMIN";
    }

    private User getUserByProviderId(RefreshToken refreshToken) {
        return userRepository.findByProviderId(refreshToken.getProviderId());
    }

    private String createAccessToken(User user, String role) {
        return jwtUtil.createAccessToken(user.getProviderId(), role);
    }
}
