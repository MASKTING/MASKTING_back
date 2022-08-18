package com.maskting.backend.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Partner {
    private String partnerLocation;
    private String partnerDuty;
    private String partnerSmoking;
    private String partnerReligion;
    private Integer partnerDrinking;
    private String partnerHeight;
    private String partnerBodyType;
}
