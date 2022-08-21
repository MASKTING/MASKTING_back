package com.maskting.backend.config.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maskting.backend.domain.RefreshToken;
import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.maskting.backend.domain.oauth.OAuth2UserInfo;
import com.maskting.backend.domain.oauth.UserPrincipal;
import com.maskting.backend.repository.RefreshTokenRepository;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
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

    private final String googleUserinfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo";
    private final String naverUserinfoUrl = "https://openapi.naver.com/v1/nid/me";
    private final String kakaoUserinfoUrl = "https://kapi.kakao.com/v2/user/me";
    private final String redirectUri = "http://localhost:3000/oauth2/redirect";

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtil cookieUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        OAuth2UserInfo oAuth2UserInfo = principal.getOAuth2UserInfo();

        if (needJoin(principal)) {
            String email = searchEmail(principal, oAuth2UserInfo);

            String url = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("role", "guest")
                    .queryParam("sort", "false")
                    .queryParam("providerId", oAuth2UserInfo.getProviderId())
                    .queryParam("provider", oAuth2UserInfo.getProvider())
                    .queryParam("email", email)
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, url);
        }

        if (!needJoin(principal)) {
            String role = returnAuthority(principal.getAuthorities());
            //TODO returnAuthority 굳이 불필요? -> principal.getUser().getRoleType
            String accessToken = jwtUtil.createAccessToken(oAuth2UserInfo.getProviderId(), role);
            response.setHeader("accessToken", accessToken);

            String key = UUID.randomUUID().toString();
            String refreshToken = jwtUtil.createRefreshToken(key);
            cookieUtil.deleteCookie(request, response, "refreshToken");
            cookieUtil.addCookie(response, "refreshToken", refreshToken, jwtUtil.getRefreshTokenValidTime());

            User user = principal.getUser();
            RefreshToken dbRefreshToken = new RefreshToken(key, user.getProviderId());
            refreshTokenRepository.save(dbRefreshToken);

            redirectByRole(request, response, user);
        }

    }

    private void redirectByRole(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        String url = null;
        if (user.getRoleType() == RoleType.GUEST) {
            url = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("role", "guest")
                    .queryParam("sort", user.isSort())
                    .queryParam("providerId", user.getProviderId())
                    .build().toUriString();
        }

        if (user.getRoleType() == RoleType.USER) {
            url = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("role", "user")
                    .queryParam("providerId", user.getProviderId())
                    .build().toUriString();
        }

        //TODO 관리자
        getRedirectStrategy().sendRedirect(request, response, url);
    }

    private String searchEmail(UserPrincipal principal, OAuth2UserInfo oAuth2UserInfo) throws JsonProcessingException {
        HttpHeaders headers = getHttpHeaders(principal);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity(headers);
        String email = getEmail(oAuth2UserInfo, httpEntity);

        return email;
    }

    private HttpHeaders getHttpHeaders(UserPrincipal principal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization","Bearer "+ principal.getAccess_token());
        return headers;
    }

    private String getEmail(OAuth2UserInfo oAuth2UserInfo, HttpEntity<MultiValueMap<String, String>> httpEntity) throws JsonProcessingException {
        JsonNode jsonNode;

        if (oAuth2UserInfo.getProvider().equals("google")) {
            jsonNode = getJsonNode(httpEntity, googleUserinfoUrl);
            return jsonNode.get("email").asText();
        }

        if (oAuth2UserInfo.getProvider().equals("naver")) {
            jsonNode = getJsonNode(httpEntity, naverUserinfoUrl);
            JsonNode response = jsonNode.path("response");
            return response.path("email").asText();
        }

        jsonNode = getJsonNode(httpEntity, kakaoUserinfoUrl);
        JsonNode kakao_account = jsonNode.path("kakao_account");
        return kakao_account.path("email").asText();
    }

    private JsonNode getJsonNode(HttpEntity<MultiValueMap<String, String>> httpEntity, String userInfoUrl) throws JsonProcessingException {
        ResponseEntity<String> responseEntity;
        JsonNode jsonNode;

        responseEntity = restTemplate.exchange(userInfoUrl, HttpMethod.GET, httpEntity, String.class);
        jsonNode = objectMapper.readTree(responseEntity.getBody());
        return jsonNode;
    }

    private boolean needJoin(UserPrincipal principal) {
        return principal.getUser() == null;
    }

    private String returnAuthority(Collection<? extends GrantedAuthority> authorities) {
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("ROLE_USER"))
                return "ROLE_USER";

            if (grantedAuthority.getAuthority().equals("ROLE_ADMIN")) {
                return "ROLE_ADMIN";
            }
        }

        return "ROLE_GUEST";
    }

}
