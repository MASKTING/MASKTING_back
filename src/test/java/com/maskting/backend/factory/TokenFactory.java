package com.maskting.backend.factory;

import com.maskting.backend.domain.User;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenFactory {
    @Autowired UserFactory userFactory;
    @Autowired UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;

    public String createAdminAccessToken() {
        User admin = userFactory.createAdmin();
        userRepository.save(admin);
        return jwtUtil.createAccessToken(admin.getProviderId(),
                "ROLE_" + admin.getRoleType().toString());
    }
}
