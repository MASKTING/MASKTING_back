package com.maskting.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReSignupResponse {

    private String name;

    private String birth;

    private int height;

    private String nickname;

    private String bio;

    private String profilePath;

    private byte[] profile;

    private String profileType;

    private String maskProfilePath;

    private byte[] maskProfile;

    private String maskProfileType;
}
