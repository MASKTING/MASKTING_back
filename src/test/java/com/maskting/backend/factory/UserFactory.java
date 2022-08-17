package com.maskting.backend.factory;

import com.maskting.backend.domain.ProviderType;
import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.maskting.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserFactory {
    @Autowired UserRepository userRepository;

    public User createUser(String name, String nickname) {
        User user = User.builder()
                .name(name)
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
                .nickname(nickname)
                .roleType(RoleType.USER)
                .providerId("testProviderId")
                .providerType(ProviderType.GOOGLE)
                .sort(false)
                .build();
        return user;
    }

    public User createGuest(String name, String nickname) {
        User user = User.builder()
                .name(name)
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
                .nickname(nickname)
                .roleType(RoleType.GUEST)
                .providerId("testProviderId")
                .providerType(ProviderType.GOOGLE)
                .sort(true)
                .profiles(new ArrayList<>())
                .build();
        return user;
    }

    public List<User> createSavedGuests(){
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            User user;
            user = getUser(i);
            userRepository.save(user);
            users.add(user);
        }
        return users;
    }

    private User getUser(int i) {
        User user;
        if (i < 10)
            user = createGuest("First", "user" + i);
        else
            user = createGuest("Second", "user" + i);
        return user;
    }

    public List<User> createGuests(){
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            User user;
            user = getUser(i);
            users.add(user);
        }
        return users;
    }

    public User createAdmin(){
        User user = User.builder()
                .name("admin")
                .email("admin@gmail.com")
                .gender("admin")
                .birth("admin")
                .location("admin")
                .occupation("admin")
                .phone("admin")
                .interest("admin")
                .drinking(0)
                .height(0)
                .bodyType(0)
                .religion("admin")
                .nickname("admin")
                .roleType(RoleType.ADMIN)
                .providerId("adminProviderId")
                .providerType(ProviderType.GOOGLE)
                .build();
        return user;
    }
}
