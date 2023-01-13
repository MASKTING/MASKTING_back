package com.maskting.backend.domain;

public enum ScreeningResult {
    PASS("pass"),
    WAIT("wait"),
    FAIl("fail");

    private String name;

    ScreeningResult(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
