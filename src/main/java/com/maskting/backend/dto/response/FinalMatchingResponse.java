package com.maskting.backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalMatchingResponse {
    private String myProfile;
    private String partnerProfile;
    private String partnerNickname;
    private String partnerNumber;
}
