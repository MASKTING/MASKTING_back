package com.maskting.backend.factory;

import com.maskting.backend.domain.Partner;
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
        User user = createUserByRole(name, nickname, RoleType.USER, false);
        return user;
    }

    private User createUserByRole(String name, String nickname, RoleType roleType, Boolean sort) {
        Partner partner = new Partner("경기 북부, 경기 중부", "any", "any",
                "무교", 1, "165, 175", "2, 4");
        return User.builder()
                .name(name)
                .email("test@gmail.com")
                .gender("male")
                .birth("19990815")
                .location("경기 북부")
                .occupation("대학생")
                .phone("01012345678")
                .interest("산책")
                .drinking(5)
                .height(181)
                .bodyType(3)
                .religion("무교")
                .nickname(nickname)
                .partner(partner)
                .roleType(roleType)
                .profiles(new ArrayList<>())
                .providerId("testProviderId")
                .providerType(ProviderType.GOOGLE)
                .sort(sort)
                .build();
    }

    public User createGuest(String name, String nickname) {
        User user = createUserByRole(name, nickname, RoleType.GUEST, true);
        return user;
    }

    public List<User> createSavedGuests(){
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            User user;
            user = createUserByRole(i);
            userRepository.save(user);
            users.add(user);
        }
        return users;
    }

    private User createUserByRole(int i) {
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
            user = createUserByRole(i);
            users.add(user);
        }
        return users;
    }

    public User createAdmin(){
        User user = User.builder()
                .name("admin")
                .email("admin@gmail.com")
                .nickname("admin")
                .roleType(RoleType.ADMIN)
                .providerId("adminProviderId")
                .providerType(ProviderType.GOOGLE)
                .build();
        return user;
    }
}
