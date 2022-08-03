package com.maskting.backend.dto.request;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
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

    @NotNull
    private String interest;

    @NotNull
    private boolean duty;

    @NotNull
    private boolean smoking;

    @NotNull
    private int drinking;

    @NotNull
    private String religion;

    @NotNull
    private String nickname;

    private List<MultipartFile> profiles;
}
