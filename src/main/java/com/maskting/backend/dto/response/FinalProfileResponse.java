package com.maskting.backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalProfileResponse {
    private String maskProfile;
    private String defaultProfile;
}
