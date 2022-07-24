package com.maskting.backend.dto.request;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

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
    private String providerId;

    @NotNull
    private String provider;

}
