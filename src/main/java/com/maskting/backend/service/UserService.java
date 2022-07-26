package com.maskting.backend.service;

import com.maskting.backend.domain.ProviderType;
import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.AdditionalSignupRequest;
import com.maskting.backend.dto.request.SignupRequest;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.common.exception.InvalidProviderException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public User joinUser(SignupRequest signupRequest) {
        ProviderType providerType = getProviderType(signupRequest);

        User user = User.builder()
                .name(signupRequest.getName())
                .email(signupRequest.getEmail())
                .gender(signupRequest.getGender())
                .birth(signupRequest.getBirth())
                .location(signupRequest.getLocation())
                .occupation(signupRequest.getOccupation())
                .phone(signupRequest.getPhone())
                .roleType(RoleType.GUEST)
                .providerId(signupRequest.getProviderId())
                .providerType(providerType)
                .build();

        userRepository.save(user);
        return user;
    }

    private ProviderType getProviderType(SignupRequest signupRequest) {
        if (signupRequest.getProvider().equals("google"))
            return ProviderType.GOOGLE;
        if (signupRequest.getProvider().equals("naver"))
            return ProviderType.NAVER;
        if (signupRequest.getProvider().equals("kakao"))
            return ProviderType.KAKAO;

        throw new InvalidProviderException();
    }

    public void returnAccessToken(HttpServletResponse response, User user) {
        String accessToken = jwtUtil.createAccessToken(user.getProviderId(), "ROLE_GUEST");
        response.setHeader("accessToken", accessToken);
    }

    @Transactional
    public void returnRefreshToken(HttpServletRequest request, HttpServletResponse response, User user) {
        String key = UUID.randomUUID().toString();
        String refreshToken = jwtUtil.createRefreshToken(key);

        cookieUtil.deleteCookie(request, response, "refreshToken");
        cookieUtil.addCookie(response, "refreshToken", refreshToken, jwtUtil.getRefreshTokenValidTime());

        RefreshToken dbRefreshToken = new RefreshToken(key, user.getProviderId());
        refreshTokenRepository.save(dbRefreshToken);
    }

    @Transactional
    public void deleteAuth(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> cookie = cookieUtil.getCookie(request, "refreshToken");
        if (cookie.isPresent()) {
            String token = cookie.get().getValue();
            String key = jwtUtil.getSubject(token);
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findById(key);

            deleteRefreshToken(refreshToken);
        }

        cookieUtil.deleteCookie(request, response, "refreshToken");
    }

    private void deleteRefreshToken(Optional<RefreshToken> refreshToken) {
        if (refreshToken.isPresent()) {
            refreshTokenRepository.delete(refreshToken.get());
        }
    }

    @Transactional
    public void addAdditionalInfo(AdditionalSignupRequest additionalSignupRequest) {
        User user = userRepository.findByProviderId(additionalSignupRequest.getProviderId());
        user.updateAdditionalInfo(additionalSignupRequest);
        user.updateSort();
    }
}
