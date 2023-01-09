package com.maskting.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReSignupRequest {

    @NotNull
    private String name;

    @NotNull
    private String birth;

    @NotNull
    private int height;

    @NotNull
    private String nickname;

    @NotNull
    private String bio;

    private List<MultipartFile> profiles;
}
