package com.maskting.backend.domain.oauth;

import com.maskting.backend.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class UserPrincipal implements OAuth2User {

    private User user;
    private OAuth2UserInfo oAuth2UserInfo;
    private Collection<GrantedAuthority> authorities;
    private String access_token;

    public UserPrincipal(User user, OAuth2UserInfo oAuth2UserInfo, String access_token) {
        this.user = user;
        this.oAuth2UserInfo = oAuth2UserInfo;
        if (user == null) {
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST"));
        } else {
            discernAuthority(user);
        }
        this.access_token = access_token;
    }

    private void discernAuthority(User user) {
        if (user.getRoleType().toString().equals("GUEST"))
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST"));
        if (user.getRoleType().toString().equals("USER"))
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        if (user.getRoleType().toString().equals("ADMIN"))
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2UserInfo.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return oAuth2UserInfo.getProviderId();
    }
}
