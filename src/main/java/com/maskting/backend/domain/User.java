package com.maskting.backend.domain;

import com.sun.istack.NotNull;
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

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String gender;

    @NotNull
    private String birth;

    @NotNull
    private String location;

    @NotNull
    private String occupation;

    @NotNull
    private String phone;

    @NotNull
    private String interest;

    private boolean duty;

    private boolean smoking;

    @NotNull
    private int drinking;

    @NotNull
    private int height;

    @NotNull
    private int bodyType;

    @NotNull
    private String religion;

    @NotNull
    @Column(unique = true)
    private String nickname;

    @NotNull
    @Embedded
    private Partner partner;

    @NotNull
    private String providerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    private boolean sort;

    @OneToMany(mappedBy = "user")
    private List<Profile> profiles = new ArrayList<>();

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
}
