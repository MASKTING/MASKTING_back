package com.maskting.backend.domain;

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
public class User extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    private String interest;

    private boolean duty;

    private boolean smoking;

    private int drinking;

    private int height;

    private int bodyType;

    private String religion;

    @NotBlank
    @Column(unique = true)
    private String nickname;

    @Embedded
    private Partner partner;

    @NotBlank
    private String providerId;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

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
