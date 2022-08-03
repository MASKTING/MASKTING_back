package com.maskting.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String birth;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String occupation;

    @Column(nullable = false)
    private String phone;

    private String interest;

    private boolean duty;

    private boolean smoking;

    private int drinking;

    private String religion;

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    private boolean sort;

    @OneToMany(mappedBy = "user")
    private List<Profile> profiles = new ArrayList<>();

    public void updateType(ProviderType providerType, RoleType roleType) {
        this.providerType = providerType;
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
}
