package com.maskting.backend.config.oauth;

import com.maskting.backend.domain.RoleType;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AccessDeniedHandler implements AccessDeniedHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String accessToken = jwtUtil.resolveToken(request);
        Authentication authentication = jwtUtil.getAuthentication(accessToken);
        String providerId = ((User) authentication.getPrincipal()).getUsername();
        com.maskting.backend.domain.User user = userRepository.findByProviderId(providerId);

        renewRole(response, authentication, user);
    }

    private void renewRole(HttpServletResponse response, Authentication authentication, com.maskting.backend.domain.User user) throws IOException {
        if (user.getRoleType() == RoleType.USER || user.getRoleType() == RoleType.ADMIN) {
            String newAccessToken =
                    jwtUtil.createAccessToken(((User) authentication.getPrincipal()).getUsername()
                    , "ROLE_" + user.getRoleType().toString());
            response.setHeader("accessToken", newAccessToken);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "home");
        }else{
            response.sendError(HttpServletResponse.SC_FORBIDDEN, String.valueOf(user.isSort()));
        }
    }
}
