package com.maskting.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private String gender;

    @NotBlank
    private String birth;

    @NotBlank
    private String location;

    @NotBlank
    private String occupation;

    @NotBlank
    private String phone;

    @NotBlank
    private String providerId;

    @NotBlank
    private String provider;

    private List<String> interests;

    @NotNull
    private boolean duty;

    @NotNull
    private boolean smoking;

    @NotNull
    private int drinking;

    @NotNull
    private int height;

    @NotNull
    private int bodyType;

    @NotBlank
    private String religion;

    @NotBlank
    private String nickname;

    private List<String> partnerLocations;

    @NotBlank
    private String partnerDuty;

    @NotBlank
    private String partnerSmoking;

    private List<String> partnerReligions;

    @NotNull
    private int partnerDrinking;

    @NotBlank
    private String partnerHeight;

    private List<Integer> partnerBodyTypes;

    private List<MultipartFile> profiles;
}
