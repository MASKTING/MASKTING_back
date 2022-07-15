package com.maskting.backend.config;

import com.maskting.backend.domain.User;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import com.maskting.common.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = jwtUtil.resolveToken(request);

        if (accessToken != null && jwtUtil.validateToken(accessToken)
                && jwtUtil.isTokenExpired(accessToken) && getUser(accessToken) != null) {
            Authentication authentication = jwtUtil.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }else{
            throw new InvalidTokenException();
        }
        filterChain.doFilter(request, response);
    }

    private User getUser(String accessToken) {
        return userRepository.findByProviderId(jwtUtil.getSubject(accessToken));
    }
}
