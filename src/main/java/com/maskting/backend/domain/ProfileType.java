package com.maskting.backend.domain;

public enum ProfileType {
    DEFAULT_PROFILE(0),
    MASK_PROFILE(1);

    private int value;

    ProfileType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
