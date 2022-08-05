package com.maskting.backend.service;

import com.maskting.backend.common.exception.NoProfileException;
import com.maskting.backend.domain.*;
import com.maskting.backend.dto.request.SignupRequest;
import com.maskting.backend.dto.response.S3Response;
import com.maskting.backend.repository.ProfileRepository;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.common.exception.InvalidProviderException;
import com.maskting.backend.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private final S3Uploader s3Uploader;
    private final ProfileRepository profileRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public User joinUser(SignupRequest signupRequest) throws IOException {
        ProviderType providerType = getProviderType(signupRequest);

        List<Profile> profiles = new ArrayList<>();
        if (!CollectionUtils.isEmpty(signupRequest.getProfiles())) {
            addProfiles(signupRequest, profiles);
        } else {
            throw new NoProfileException();
        }

        signupRequest.setProfiles(null);
        User user = createUser(signupRequest, providerType, profiles);

        return userRepository.save(user);
    }

    private User createUser(SignupRequest signupRequest, ProviderType providerType, List<Profile> profiles) {
        User user = modelMapper.map(signupRequest, User.class);
        user.updateType(providerType, RoleType.GUEST);
        user.addProfiles(profiles);
        user.updateSort();
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

    private void addProfiles(SignupRequest signupRequest, List<Profile> profiles) throws IOException {
        for (MultipartFile multipartFile : signupRequest.getProfiles()) {
            S3Response s3Response = s3Uploader.upload(multipartFile, "static");

            Profile profile = Profile.builder()
                    .name(s3Response.getName())
                    .path(s3Response.getPath())
                    .build();

            profiles.add(profileRepository.save(profile));
        }
    }
}
