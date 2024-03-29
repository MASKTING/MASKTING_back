package com.maskting.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private String name;
    private String birth;
    private int height;
    private String phone;
    private String nickname;
    private String bio;
    private List<String> profiles;
}
