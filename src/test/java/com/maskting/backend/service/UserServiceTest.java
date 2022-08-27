package com.maskting.backend.service;

import com.maskting.backend.domain.*;
import com.maskting.backend.dto.request.SignupRequest;
import com.maskting.backend.dto.response.S3Response;
import com.maskting.backend.factory.RequestFactory;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.*;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.util.S3Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserFactory userFactory;
    private RequestFactory requestFactory;

    @InjectMocks
    UserService userService;

    @Mock
    S3Uploader s3Uploader;

    @Mock
    ProfileRepository profileRepository;

    @Mock
    ModelMapper modelMapper;

    @Mock
    UserRepository userRepository;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    CookieUtil cookieUtil;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    InterestRepository interestRepository;

    @Mock
    PartnerLocationRepository partnerLocationRepository;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        requestFactory = new RequestFactory();
    }

    @Test
    @DisplayName("회원 가입")
    void joinUser() throws IOException {
        SignupRequest signupRequest = requestFactory.createSignupRequest();
        Profile profile = Profile.builder()
                .name("testName")
                .path("testPath")
                .build();
        Interest interest = Interest.builder()
                .name("산책")
                .build();
        PartnerLocation partnerLocation = PartnerLocation.builder()
                .name("경기 북부")
                .build();
        S3Response s3Response = new S3Response("testName", "testPath");
        given(s3Uploader.upload(any(MultipartFile.class), anyString())).willReturn(s3Response);
        User user = userFactory.createUser("test","알콜쟁이 라이언");
        given(modelMapper.map(signupRequest, User.class)).willReturn(user);
        given(profileRepository.save(any())).willReturn(profile);
        given(userRepository.save(any())).willReturn(user);
        given(interestRepository.save(any())).willReturn(interest);
        given(partnerLocationRepository.save(any())).willReturn(partnerLocation);

        User joinUser = userService.joinUser(signupRequest);

        assertNotNull(joinUser);
        assertEquals(1, joinUser.getProfiles().size());
        assertEquals(1, joinUser.getInterests().size());
        assertEquals(1, joinUser.getPartnerLocations().size());
        assertTrue(joinUser.isSort());
    }

    @Test
    @DisplayName("헤더에 액세스 토큰 저장")
    void returnAccessToken() {
        HttpServletResponse response = new MockHttpServletResponse();
        given(jwtUtil.createAccessToken(anyString(), anyString())).willReturn("testAccessToken");
        User user = userFactory.createUser("test","알콜쟁이 라이언");

        userService.returnAccessToken(response, user);

        assertNotNull(response.getHeader("accessToken"));
    }

    @Test
    @DisplayName("쿠키, db에 리프레쉬 토큰 저장")
    void returnRefreshToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        User user = userFactory.createUser("test","알콜쟁이 라이언");
        given(jwtUtil.createRefreshToken(anyString())).willReturn("testRefreshToken");

        userService.returnRefreshToken(request, response, user);

        verify(cookieUtil).deleteCookie(request, response, "refreshToken");
        verify(cookieUtil).addCookie(response, "refreshToken", "testRefreshToken", jwtUtil.getRefreshTokenValidTime());
        verify(refreshTokenRepository).save(any());
    }

    @Test
    @DisplayName("쿠키, db에 리프레쉬 토큰 삭제")
    void deleteAuth() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        Cookie cookie = createCookie();
        given(cookieUtil.getCookie(request, "refreshToken")).willReturn(Optional.of(cookie));
        RefreshToken refreshToken = new RefreshToken("testId", "testProviderId");
        given(jwtUtil.getSubject(anyString())).willReturn("testKey");
        given(refreshTokenRepository.findById("testKey")).willReturn(Optional.of(refreshToken));

        userService.deleteAuth(request, response);

        verify(refreshTokenRepository).delete(any());
        verify(cookieUtil).deleteCookie(request, response, "refreshToken");
    }

    private Cookie createCookie() {
        Cookie cookie = new Cookie("refreshToken", "testValue");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(jwtUtil.getRefreshTokenValidTime());
        return cookie;
    }
}