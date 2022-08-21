package com.maskting.backend.factory;

import com.maskting.backend.dto.request.SignupRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class RequestFactory {

    public SignupRequest createSignupRequest() {
        List<MultipartFile> profiles = List.of(
                new MockMultipartFile("profiles", "test.PNG", MediaType.IMAGE_PNG_VALUE, "test".getBytes())
        );
        SignupRequest signupRequest = new SignupRequest(
                "test", "test@gmail.com", "male",
                "19990815", "경기 북부", "대학생",
                "01012345678", "testProviderId", "google",
                "산책", true, false,
                5, 181, 3,
                "무교", "알콜쟁이 라이언", "경기 북부, 경기 중부", "any", "any",
                "무교", 1, "165, 175", "2, 4", profiles);
        return signupRequest;
    }
}
