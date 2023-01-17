package com.maskting.backend.factory;

import com.maskting.backend.domain.*;
import com.maskting.backend.domain.oauth.*;
import com.maskting.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class UserFactory {
    @Autowired UserRepository userRepository;

    public User createUser(String name, String nickname) {
        User user = createUserByRole(name, nickname, RoleType.USER, false);
        return user;
    }

    private User createUserByRole(String name, String nickname, RoleType roleType, Boolean sort) {
        Partner partner = new Partner("any", "any",
                1, 160, 170);
        return User.builder()
                .name(name)
                .email("test@gmail.com")
                .gender("male")
                .birth("19990815")
                .location("경기 북부")
                .occupation("대학생")
                .phone("01012345678")
                .interests(new ArrayList<>())
                .drinking(5)
                .height(181)
                .bodyType(3)
                .religion("무교")
                .nickname(nickname)
                .partner(partner)
                .partnerLocations(new ArrayList<>())
                .partnerReligions(new ArrayList<>())
                .partnerBodyTypes(new ArrayList<>())
                .roleType(roleType)
                .profiles(getProfiles())
                .feeds(new ArrayList<>())
                .activeMatcher(new ArrayList<>())
                .passiveMatcher(new ArrayList<>())
                .activeExclusioner(new ArrayList<>())
                .passiveExclusioner(new ArrayList<>())
                .following(new ArrayList<>())
                .follower(new ArrayList<>())
                .providerId("providerId_" + nickname)
                .providerType(ProviderType.GOOGLE)
                .sort(sort)
                .bio("자기소개")
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
        Partner partner = new Partner(0);
        User user = User.builder()
                .name("admin")
                .email("admin@gmail.com")
                .nickname("admin")
                .roleType(RoleType.ADMIN)
                .providerId("adminProviderId")
                .providerType(ProviderType.GOOGLE)
                .partner(partner)
                .build();
        return user;
    }

    public UserPrincipal createGoogleUser(User user) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "123456789");
        OAuth2UserInfo oAuth2UserInfo = new GoogleUserInfo(attributes);
        return new UserPrincipal(user, oAuth2UserInfo, "accessToken");
    }

    public Authentication createAuthentication(UserPrincipal userPrincipal) {
        Authentication authentication = new Authentication() {
            @Override
            public boolean equals(Object another) {
                return false;
            }

            @Override
            public String toString() {
                return null;
            }

            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return userPrincipal;
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }
        };
        return authentication;
    }

    public UserPrincipal createNaverUser(User user) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "123456789");
        OAuth2UserInfo oAuth2UserInfo = new NaverUserInfo(attributes);
        return new UserPrincipal(user, oAuth2UserInfo, "accessToken");
    }

    public UserPrincipal createKakaoUser(User user) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "123456789");
        OAuth2UserInfo oAuth2UserInfo = new KakaoUserInfo(attributes);
        return new UserPrincipal(user, oAuth2UserInfo, "accessToken");
    }

    public User createFemaleUser(String nickname) {
        Partner partner = new Partner("any", "any",
                1, 175, 180);
        return User.builder()
                .name("test")
                .email("test@gmail.com")
                .gender("female")
                .birth("19990815")
                .location("경기 북부")
                .occupation("대학생")
                .phone("01012345678")
                .interests(new ArrayList<>())
                .drinking(5)
                .height(165)
                .bodyType(3)
                .religion("무교")
                .nickname(nickname)
                .partner(partner)
                .partnerLocations(new ArrayList<>())
                .partnerReligions(new ArrayList<>())
                .partnerBodyTypes(new ArrayList<>())
                .roleType(RoleType.USER)
                .profiles(new ArrayList<>(getProfiles()))
                .feeds(new ArrayList<>())
                .activeMatcher(new ArrayList<>())
                .passiveMatcher(new ArrayList<>())
                .activeExclusioner(new ArrayList<>())
                .passiveExclusioner(new ArrayList<>())
                .following(new ArrayList<>())
                .follower(new ArrayList<>())
                .providerId(nickname)
                .providerType(ProviderType.GOOGLE)
                .sort(false)
                .bio("자기소개")
                .build();
    }

    public User getFemaleUserByInterests(String nickname, String interest1, String interest2) {
        User user = createFemaleUser(nickname);
        addInterests(user, new ArrayList<>(Arrays.asList(interest1, interest2)));
        return user;
    }

    public void addInterests(User user, List<String> names) {
        List<Interest> interests = new ArrayList<>();
        for (String name : names) {
            interests.add(getInterest((name)));
        }
        user.addInterests(interests);
    }

    private Interest getInterest(String name) {
        return Interest.builder()
                .name(name)
                .build();
    }

    private List<Profile> getProfiles() {
        return Arrays.asList(getProfile("https://amazon.com/DEFAULT_PROFILE.png", "DEFAULT_PROFILE"),
                getProfile("https://amazon.com/MASK_PROFILE.png", "MASK_PROFILE"));
    }

    private Profile getProfile(String path, String name) {
        return Profile.builder()
                .path(path)
                .name(name)
                .build();
    }
}
