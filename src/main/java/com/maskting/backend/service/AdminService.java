package com.maskting.backend.service;

import com.maskting.backend.domain.Profile;
import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.ReviewRequest;
import com.maskting.backend.dto.response.ReviewResponse;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public User getUser(HttpServletRequest request) {
        String accessToken = jwtUtil.resolveToken(request);
        String providerId = jwtUtil.getSubject(accessToken);
        User user = userRepository.findByProviderId(providerId);
        return user;
    }

    public List<User> findSortingUserByName(ReviewRequest reviewRequest, String name) {
        PageRequest pageRequest = getPageRequest(reviewRequest);
        List<User> guests = userRepository.findByNameContains(name, pageRequest).getContent();
        return guests;
    }

    private PageRequest getPageRequest(ReviewRequest reviewRequest) {
        int start = reviewRequest.getStart();
        int length = reviewRequest.getLength();
        return PageRequest.of(start, length);
    }

    public List<User> findSortingUser(ReviewRequest reviewRequest) {
        PageRequest pageRequest = getPageRequest(reviewRequest);
        List<User> guests = userRepository.findBySort(true, pageRequest).getContent();
        return guests;
    }

    public List<ReviewResponse> returnReviewResponse(List<User> guests) {
        List<ReviewResponse> reviewResponses = new ArrayList<>();
        for (User user : guests) {
            ReviewResponse reviewResponse = modelMapper.map(user, ReviewResponse.class);
            reviewResponse.setProfiles(new ArrayList<>());
            handleProfile(user, reviewResponse);
            reviewResponses.add(reviewResponse);
        }
        return reviewResponses;
    }

    private void handleProfile(User user, ReviewResponse reviewResponse) {
        for (Profile profile : user.getProfiles()) {
            reviewResponse.getProfiles().add(profile.getPath());
        }

        int profileCnt = reviewResponse.getProfiles().size();
        for (int i = profileCnt; i < 3; i++){
            reviewResponse.getProfiles().add("x");
        }
    }

    @Transactional
    public void convertToUser(User user) {
        user.updateSort();
        user.updateRoleType(RoleType.USER);
    }
}
