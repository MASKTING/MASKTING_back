package com.maskting.backend.service;

import com.maskting.backend.common.exception.ExistNicknameException;
import com.maskting.backend.common.exception.NoCertificationException;
import com.maskting.backend.common.exception.NoProfileException;
import com.maskting.backend.domain.*;
import com.maskting.backend.dto.request.ReSignupRequest;
import com.maskting.backend.dto.request.SignupRequest;
import com.maskting.backend.dto.response.ReSignupResponse;
import com.maskting.backend.dto.response.S3Response;
import com.maskting.backend.dto.response.SignUpRejectResponse;
import com.maskting.backend.repository.*;
import com.maskting.backend.util.CookieUtil;
import com.maskting.backend.util.JwtUtil;
import com.maskting.backend.common.exception.InvalidProviderException;
import com.maskting.backend.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final S3Uploader s3Uploader;
    private final ProfileRepository profileRepository;
    private final ModelMapper modelMapper;
    private final InterestRepository interestRepository;
    private final PartnerLocationRepository partnerLocationRepository;
    private final PartnerReligionRepository partnerReligionRepository;
    private final PartnerBodyTypeRepository partnerBodyTypeRepository;

    @Transactional
    public User joinUser(SignupRequest signupRequest) throws IOException {
        checkCertification(signupRequest);
        checkNickname(signupRequest);
        return userRepository.save(createUser(signupRequest, getProviderType(signupRequest), getProfiles(signupRequest.getProfiles())));
    }

    private void checkNickname(SignupRequest signupRequest) {
        if (userRepository.findByNickname(signupRequest.getNickname()).isPresent()) {
            throw new ExistNicknameException();
        }
    }

    private void checkCertification(SignupRequest signupRequest) {
        if (!signupRequest.isCertification()) {
            throw new NoCertificationException();
        }
    }

    private List<Profile> getProfiles(List<MultipartFile> profiles) throws IOException {
        if (haveProfiles(profiles)) {
            return addProfiles(profiles);
        }
        throw new NoProfileException();
    }

    private boolean haveProfiles(List<MultipartFile> profiles) {
        return !CollectionUtils.isEmpty(profiles);
    }

    private User createUser(SignupRequest signupRequest, ProviderType providerType, List<Profile> profiles) {
        User user = modelMapper.map(signupRequest, User.class);
        user.updateType(providerType, RoleType.GUEST);
        user.addProfiles(profiles);
        user.updatePartner(getPartner(signupRequest));
        user.addInterests(getInterests(signupRequest));
        user.addPartnerLocations(getPartnerLocations(signupRequest));
        user.addPartnerReligions(getPartnerReligions(signupRequest));
        user.addPartnerBodyTypes(getPartnerBodyTypes(signupRequest));
        user.updateSort();
        return user;
    }

    private Partner getPartner(SignupRequest signupRequest) {
        return modelMapper.map(signupRequest, Partner.class);
    }

    private List<PartnerBodyType> getPartnerBodyTypes(SignupRequest signupRequest) {
        List<PartnerBodyType> partnerBodyTypes = new ArrayList<>();
        for (Integer getPartnerBodyType : signupRequest.getPartnerBodyTypes()) {
            partnerBodyTypes.add(savePartnerBodyType(getPartnerBodyType));
        }
        return partnerBodyTypes;
    }

    private PartnerBodyType savePartnerBodyType(Integer getPartnerBodyType) {
        return partnerBodyTypeRepository.save(buildPartnerBodyType(getPartnerBodyType));
    }

    private PartnerBodyType buildPartnerBodyType(Integer getPartnerBodyType) {
        return PartnerBodyType.builder()
                .val(getPartnerBodyType)
                .build();
    }

    private List<PartnerReligion> getPartnerReligions(SignupRequest signupRequest) {
        List<PartnerReligion> partnerReligions = new ArrayList<>();
        for (String getPartnerReligion : signupRequest.getPartnerReligions()) {
            partnerReligions.add(savePartnerReligion(getPartnerReligion));
        }
        return partnerReligions;
    }

    private PartnerReligion savePartnerReligion(String getPartnerReligion) {
        return partnerReligionRepository.save(buildPartnerReligion(getPartnerReligion));
    }

    private PartnerReligion buildPartnerReligion(String getPartnerReligion) {
        return PartnerReligion.builder()
                .name(getPartnerReligion)
                .build();
    }

    private List<PartnerLocation> getPartnerLocations(SignupRequest signupRequest) {
        List<PartnerLocation> partnerLocations = new ArrayList<>();
        for (String getPartnerLocation : signupRequest.getPartnerLocations()) {
            partnerLocations.add(savePartnerLocation(getPartnerLocation));
        }
        return partnerLocations;
    }

    private PartnerLocation savePartnerLocation(String getPartnerLocation) {
        return partnerLocationRepository.save(buildPartnerLocation(getPartnerLocation));
    }

    private PartnerLocation buildPartnerLocation(String getPartnerLocation) {
        return PartnerLocation.builder()
                .name(getPartnerLocation)
                .build();
    }

    private List<Interest> getInterests(SignupRequest signupRequest) {
        List<Interest> interests = new ArrayList<>();
        for (String getInterest : signupRequest.getInterests()) {
            interests.add(saveInterest(getInterest));
        }
        return interests;
    }

    private Interest saveInterest(String getInterest) {
        return interestRepository.save(buildInterest(getInterest));
    }

    private Interest buildInterest(String getInterest) {
        return Interest.builder()
                .name(getInterest)
                .build();
    }

    private ProviderType getProviderType(SignupRequest signupRequest) {
        if (signupRequest.getProvider().equals("google"))
            return ProviderType.GOOGLE;
        if (signupRequest.getProvider().equals("naver"))
            return ProviderType.NAVER;
        if (signupRequest.getProvider().equals("kakao"))
            return ProviderType.KAKAO;

        throw new InvalidProviderException();
    }

    public void returnAccessToken(HttpServletResponse response, User user) {
        response.setHeader("accessToken", createAccessToken(user));
    }

    private String createAccessToken(User user) {
        return jwtUtil.createAccessToken(user.getProviderId(), "ROLE_GUEST");
    }

    @Transactional
    public void returnRefreshToken(HttpServletRequest request, HttpServletResponse response, User user) {
        String key = UUID.randomUUID().toString();

        cookieUtil.deleteCookie(request, response, "refreshToken");
        cookieUtil.addCookie(response, "refreshToken", createRefreshToken(key), getRefreshTokenValidTime());
        refreshTokenRepository.save(createRefreshTokenEntity(user, key));
    }

    private RefreshToken createRefreshTokenEntity(User user, String key) {
        return new RefreshToken(key, user.getProviderId());
    }

    private int getRefreshTokenValidTime() {
        return jwtUtil.getRefreshTokenValidTime();
    }

    private String createRefreshToken(String key) {
        return jwtUtil.createRefreshToken(key);
    }

    @Transactional
    public void deleteAuth(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> cookie = cookieUtil.getCookie(request, "refreshToken");
        if (cookie.isPresent()) {
            deleteRefreshToken(getRefreshToken(cookie));
            cookieUtil.deleteCookie(request, response, "refreshToken");
        }
    }

    private Optional<RefreshToken> getRefreshToken(Optional<Cookie> cookie) {
        return refreshTokenRepository.findById(getIdFromRefreshToken(cookie));
    }

    private String getIdFromRefreshToken(Optional<Cookie> cookie) {
        return jwtUtil.getSubject(getKeyFromCookie(cookie));
    }

    private String getKeyFromCookie(Optional<Cookie> cookie) {
        return cookie.get().getValue();
    }

    private void deleteRefreshToken(Optional<RefreshToken> refreshToken) {
        if (refreshToken.isPresent()) {
            refreshTokenRepository.delete(refreshToken.get());
        }
    }

    private List<Profile> addProfiles(List<MultipartFile> requestProfiles) throws IOException {
        List<Profile> profiles = new ArrayList<>();
        for (MultipartFile multipartFile : requestProfiles) {
            profiles.add(saveProfile(upload(multipartFile)));
        }
        return profiles;
    }

    private S3Response upload(MultipartFile multipartFile) throws IOException {
        return s3Uploader.upload(multipartFile, "static");
    }

    private Profile saveProfile(S3Response s3Response) {
        return profileRepository.save(buildProfile(s3Response));
    }

    private Profile buildProfile(S3Response s3Response) {
        return Profile.builder()
                .name(s3Response.getName())
                .path(s3Response.getPath())
                .build();
    }

    public boolean checkNickname(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    public SignUpRejectResponse getRejection(org.springframework.security.core.userdetails.User userDetail) {
        User user = getUserByProviderId(userDetail);
        return new SignUpRejectResponse(user.getRejection());
    }

    private User getUserByProviderId(org.springframework.security.core.userdetails.User userDetail) {
        return userRepository.findByProviderId(userDetail.getUsername());
    }

    public ReSignupResponse getReSignupInfo(org.springframework.security.core.userdetails.User userDetail) throws IOException {
        User user = getUserByProviderId(userDetail);
        return getReSignupResponse(user);
    }

    private ReSignupResponse getReSignupResponse(User user) throws IOException {
        ReSignupResponse reSignupResponse = modelMapper.map(user, ReSignupResponse.class);
        List<Profile> profiles = user.getProfiles();
        updateProfile(reSignupResponse, profiles);
        return reSignupResponse;
    }

    private void updateProfile(ReSignupResponse reSignupResponse, List<Profile> profiles) throws IOException {
        reSignupResponse.setProfilePath(getS3Path(profiles, ProfileType.DEFAULT_PROFILE));
        String s3ProfileName = getS3Name(profiles, ProfileType.DEFAULT_PROFILE);
        reSignupResponse.setProfile(s3Uploader.download(s3ProfileName));
        reSignupResponse.setProfileType(getImageType(s3ProfileName));
        reSignupResponse.setMaskProfilePath(getS3Path(profiles, ProfileType.MASK_PROFILE));
        String s3MaskProfileName = getS3Name(profiles, ProfileType.MASK_PROFILE);
        reSignupResponse.setMaskProfile((s3Uploader.download(s3MaskProfileName)));
        reSignupResponse.setMaskProfileType(getImageType(s3MaskProfileName));
    }

    private String getImageType(String s3ProfileName) {
        String[] split = s3ProfileName.split("\\.");
        return split[split.length - 1];
    }

    private String getS3Path(List<Profile> profiles, ProfileType profileType) {
        return profiles.get(profileType.getValue()).getPath();
    }

    private String getS3Name(List<Profile> profiles, ProfileType profileType) {
        return profiles.get(profileType.getValue()).getName();
    }

    @Transactional
    public void reSignup(org.springframework.security.core.userdetails.User userDetail, ReSignupRequest reSignupRequest) throws IOException {
        User user = getUserByProviderId(userDetail);
        user.reUpdate(reSignupRequest);
        deleteProfiles(user);
        user.addProfiles(getProfiles(reSignupRequest.getProfiles()));
        user.updateSort();
    }

    private void deleteProfiles(User user) {
        for (Profile profile : user.getProfiles()) {
            profileRepository.delete(profile);
            s3Uploader.delete(profile.getName());
        }
    }

    public String getScreeningResult(org.springframework.security.core.userdetails.User userDetail) {
        User user = getUserByProviderId(userDetail);
        return getResultByUserType(user);
    }

    private String getResultByUserType(User user) {
        if (user.getRoleType().equals(RoleType.USER)){
            return ScreeningResult.PASS.getName();
        }
        if (isWait(user)) {
            return ScreeningResult.WAIT.getName();
        }
        return ScreeningResult.FAIl.getName();
    }

    private boolean isWait(User user) {
        return user.getRoleType().equals(RoleType.GUEST) && user.isSort();
    }
}
