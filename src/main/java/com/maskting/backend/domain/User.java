package com.maskting.backend.domain;

import com.maskting.backend.dto.request.ReSignupRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`user`")
public class User extends BaseTimeEntity{

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    private String gender;

    private String birth;

    private String location;

    private String occupation;

    private String phone;

    @OneToMany(mappedBy = "user")
    private List<Interest> interests = new ArrayList<>();

    private boolean duty;

    private boolean smoking;

    private int drinking;

    private int height;

    private int bodyType;

    private String religion;

    private String bio;

    @NotBlank
    @Column(unique = true)
    private String nickname;

    @Embedded
    private Partner partner;

    @OneToMany(mappedBy = "user")
    private List<PartnerLocation> partnerLocations = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<PartnerReligion> partnerReligions = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<PartnerBodyType> partnerBodyTypes = new ArrayList<>();

    @NotBlank
    private String providerId;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    private boolean sort;

    @OneToMany(mappedBy = "user")
    private List<Profile> profiles = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Feed> feeds = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MATCH_ID")
    private User match;

    @OneToMany(mappedBy = "match")
    private List<User> matches = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXCLUSION_ID")
    private User exclusion;

    @OneToMany(mappedBy = "exclusion")
    private List<User> exclusions = new ArrayList<>();

    private boolean latest;

    @OneToMany(mappedBy = "following")
    private List<Follow> following = new ArrayList<>();

    @OneToMany(mappedBy = "follower")
    private List<Follow> follower = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ChatUser> chatUsers = new ArrayList<>();

    private String rejection;

    public void updateType(ProviderType providerType, RoleType roleType) {
        this.providerType = providerType;
        updateRoleType(roleType);
    }

    public void updateRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public void addProfiles(List<Profile> profiles) {
        this.profiles = new ArrayList<>();
        for (Profile profile : profiles) {
            this.profiles.add(profile);
            profile.updateUser(this);
        }
    }

    public void updateSort() {
        this.sort = !isSort();
    }

    public void updatePartner(Partner partner) {
        this.partner = partner;
    }

    public void addInterests(List<Interest> interests) {
        this.interests = new ArrayList<>();
        for (Interest interest : interests) {
            this.interests.add(interest);
            interest.updateUser(this);
        }
    }

    public void addPartnerLocations(List<PartnerLocation> partnerLocations) {
        this.partnerLocations = new ArrayList<>();
        for (PartnerLocation partnerLocation : partnerLocations) {
            this.partnerLocations.add(partnerLocation);
            partnerLocation.updateUser(this);
        }
    }

    public void addPartnerReligions(List<PartnerReligion> partnerReligions) {
        this.partnerReligions = new ArrayList<>();
        for (PartnerReligion partnerReligion : partnerReligions) {
            this.partnerReligions.add(partnerReligion);
            partnerReligion.updateUser(this);
        }
    }

    public void addPartnerBodyTypes(List<PartnerBodyType> partnerBodyTypes) {
        this.partnerBodyTypes = new ArrayList<>();
        for (PartnerBodyType partnerBodyType : partnerBodyTypes) {
            this.partnerBodyTypes.add(partnerBodyType);
            partnerBodyType.updateUser(this);
        }
    }

    public void updateMatches(List<User> partners) {
        for (User user : partners) {
            matches.add(user);
            user.updateMatch(this);
        }
    }

    private void updateMatch(User user) {
        match = user;
    }

    public void updateLatest() {
        latest = !latest;
    }

    public void updateExclusions(List<User> matches) {
        for (User user : matches) {
            exclusions.add(user);
            user.updateExclusion(this);
            user.updateMatch(null);
        }
    }

    private void updateExclusion(User user) {
        exclusion = user;
    }

    public void addLike(Follow follow) {
        following.add(follow);
    }

    public void updateRejection(String reason) {
        rejection = reason;
    }

    public void reUpdate(ReSignupRequest reSignupRequest) {
        name = reSignupRequest.getName();
        birth = reSignupRequest.getBirth();
        height = reSignupRequest.getHeight();
        nickname = reSignupRequest.getNickname();
        bio = reSignupRequest.getBio();
    }
}
