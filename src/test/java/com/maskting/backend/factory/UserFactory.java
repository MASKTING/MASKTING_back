package com.maskting.backend.factory;

import com.maskting.backend.domain.ProviderType;
import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    public User createUser() {
        User user = User.builder()
                .name("test")
                .email("test@gmail.com")
                .gender("male")
                .birth("19990815")
                .location("서울 강북구")
                .occupation("대학생")
                .phone("01012345678")
                .interest("산책")
                .drinking(5)
                .height(181)
                .bodyType(3)
                .religion("무교")
                .nickname("알콜쟁이 라이언")
                .roleType(RoleType.USER)
                .providerId("testProviderId")
                .providerType(ProviderType.GOOGLE)
                .build();
        return user;
    }
}
