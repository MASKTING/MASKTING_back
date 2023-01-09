package com.maskting.backend.factory;

import com.maskting.backend.dto.request.ReSignupRequest;
import com.maskting.backend.dto.request.SignupRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Component
public class RequestFactory {

    public SignupRequest createSignupRequest() {
        List<MultipartFile> profiles = getProfiles();

        List<String> interests = new ArrayList<>();
        interests.add("산책");

        List<String> partnerLocations = new ArrayList<>();
        partnerLocations.add("경기 북부");

        List<String> partnerReligions = new ArrayList<>();
        partnerReligions.add("무교");

        List<Integer> partnerBodyTypes = new ArrayList<>();
        partnerBodyTypes.add(2);

        SignupRequest signupRequest = new SignupRequest(
                "test", "test@gmail.com", "male",
                "19990815", "경기 북부", "대학생",
                "01012345678", "testProviderId", "google",
                interests, true, false,
                5, 181, 3,
                "무교", "알콜쟁이 라이언", partnerLocations, "any", "any",
                partnerReligions, 1, 160, 170, "운동 좋아합니다!", partnerBodyTypes, profiles);
        return signupRequest;
    }

    private List<MultipartFile> getProfiles() {
        return List.of(
                new MockMultipartFile("profiles", "DEFAULT_IMAGE.PNG", MediaType.IMAGE_PNG_VALUE, "DEFAULT_IMAGE".getBytes()),
                new MockMultipartFile("profiles", "MASK_IMAGE.PNG", MediaType.IMAGE_PNG_VALUE, "MASK_IMAGE".getBytes())
        );
    }

    public ReSignupRequest createReSignupRequest() {
        List<MultipartFile> profiles = getProfiles();
        return new ReSignupRequest("홍길동", "19921123", 181, "심심한 무지", "안녕하세요~", profiles);
    }
}
