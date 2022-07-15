package com.maskting.backend.config.oauth;

import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.oauth.OAuth2UserInfo;
import com.maskting.backend.domain.oauth.UserPrincipal;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtil cookieUtil;

    @Value("${app.auth.redirectUri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        OAuth2UserInfo oAuth2UserInfo = principal.getOAuth2UserInfo();

        if (needJoin(principal)) {
            String url = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("flag", "guest")
                    .queryParam("providerId", oAuth2UserInfo.getProviderId())
                    .queryParam("provider", oAuth2UserInfo.getProvider())
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, url);
        }

        if (!needJoin(principal)) {
            String role = hasAuthority(principal.getAuthorities(), "ROLE_USER") ? "ROLE_USER" : "ROLE_ADMIN";
            String accessToken = jwtUtil.createAccessToken(oAuth2UserInfo.getProviderId(), role);
            String key = UUID.randomUUID().toString();
            String refreshToken = jwtUtil.createRefreshToken(key);

            cookieUtil.deleteCookie(request, response, "refreshToken");
            cookieUtil.addCookie(response, "refreshToken", refreshToken, jwtUtil.getRefreshTokenValidTime());

            RefreshToken dbRefreshToken = new RefreshToken(key, principal.getUser().getProviderId());
            refreshTokenRepository.save(dbRefreshToken);
            
            String url = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("flag", "user")
                    .queryParam("accessToken", accessToken)
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, url);
        }

    }

    private boolean needJoin(UserPrincipal principal) {
        return principal.getUser() == null;
    }

    private boolean hasAuthority(Collection<? extends GrantedAuthority> authorities, String authority) {
        if (authorities == null) {
            return false;
        }

        for (GrantedAuthority grantedAuthority : authorities) {
            if (authority.equals(grantedAuthority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

}