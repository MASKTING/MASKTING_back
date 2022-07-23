package com.maskting.backend.service.oauth;

import com.maskting.backend.domain.User;
import com.maskting.backend.domain.oauth.*;
import com.maskting.backend.repository.UserRepository;
import com.maskting.common.exception.oauth.ProviderMissMatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            return this.process(userRequest, oAuth2User, userRequest.getAccessToken().getTokenValue());
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage(),e.getCause());
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User oAuth2User, String accessToken) {
        String platform = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = distinguishedByPlatform(oAuth2User, platform);

        User user = userRepository.findByProviderId(userInfo.getProviderId());

        if (user != null) {
            missMatchProvider(platform, user);
        }

        return new UserPrincipal(user, userInfo, accessToken);
    }

    private void missMatchProvider(String platform, User user) {
        if (!platform.equals(user.getProviderType().toString().toLowerCase(Locale.ROOT)))
            throw new ProviderMissMatchException();
    }

    private OAuth2UserInfo distinguishedByPlatform(OAuth2User oAuth2User, String flatForm) {
        if (flatForm.equals("google")) {
            return new GoogleUserInfo(oAuth2User.getAttributes());
        }
        if (flatForm.equals("naver")) {
            return new NaverUserInfo((Map) oAuth2User.getAttributes().get("response"));
        }
        if (flatForm.equals("kakao")) {
            return new KakaoUserInfo(oAuth2User.getAttributes());
        }

        throw new OAuth2AuthenticationException("잘못된 provider 입니다.");
    }
}
