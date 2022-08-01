package com.maskting.backend.dto.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalSignupRequest {

    private String providerId;

    private String interest;

    private boolean duty;

    private boolean smoking;

    private int drinking;

    private String religion;

    private String nickname;

    private List<MultipartFile> profiles;
}
