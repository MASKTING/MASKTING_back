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
    private String partnerDuty;
    private String partnerSmoking;
    private int partnerDrinking;
    private int partnerMinHeight;
    private int partnerMaxHeight;

    public Partner(int partnerDrinking) {
        this.partnerDrinking = partnerDrinking;
    }
}
