package com.maskting.backend.service;

import com.maskting.backend.domain.RoleType;
import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.ReviewRequest;
import com.maskting.backend.dto.response.ReviewResponse;
import com.maskting.backend.factory.UserFactory;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    private UserFactory userFactory;

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
    }

    @Test
    @DisplayName("액세스토큰에서 유저 반환 ")
    void getUser() {
        User user = userFactory.createAdmin();
        HttpServletRequest request = mock(HttpServletRequest.class);
        given(jwtUtil.resolveToken(request)).willReturn("accessToken");
        given(jwtUtil.getSubject(anyString())).willReturn("adminProviderId");
        given(userRepository.findByProviderId("adminProviderId")).willReturn(user);

        User getUser = adminService.getAdmin(request);

        verify(userRepository).findByProviderId(anyString());
        assertEquals("admin", getUser.getName());
        assertEquals(RoleType.ADMIN, getUser.getRoleType());
    }

    @Test
    @DisplayName("이름기반 검색 페이징 유저 반환")
    void findSortingUserByName() {
        List<User> allUser = userFactory.createGuests();
        List<User> filterUser = new ArrayList<>();
        SortUser(allUser, filterUser);
        ReviewRequest reviewRequest = new ReviewRequest(1, 0, 10);
        int page = reviewRequest.getStart() / reviewRequest.getLength();
        PageRequest pageRequest = PageRequest.of(page, reviewRequest.getLength());
        Page<User> pagingUser = new PageImpl<>(filterUser);
        given(userRepository.findByNameContains("Second", pageRequest)).willReturn(pagingUser);

        List<User> guests = adminService.findSortingUserByName(reviewRequest, "Second");

        verify(userRepository).findByNameContains(anyString(), any());
        assertEquals(5, guests.size());
    }

    private void SortUser(List<User> allUser, List<User> filterUser) {
        for (User user : allUser) {
            if (user.getName().equals("Second"))
                filterUser.add(user);
        }
    }

    @Test
    @DisplayName("페이징 유저 반환")
    void findSortingUser() {
        List<User> allUser = userFactory.createGuests();
        List<User> filterUser = new ArrayList<>();
        pagingUser(allUser, filterUser);
        ReviewRequest reviewRequest = new ReviewRequest(1, 0, 10);
        int page = reviewRequest.getStart() / reviewRequest.getLength();
        Page<User> pagingUser = new PageImpl<>(filterUser);
        PageRequest pageRequest = PageRequest.of(page, reviewRequest.getLength());
        given(userRepository.findBySort(true, pageRequest)).willReturn(pagingUser);

        List<User> guests = adminService.findSortingUser(reviewRequest);

        verify(userRepository).findBySort(true, pageRequest);
        assertEquals(10, guests.size());
    }

    private void pagingUser(List<User> allUser, List<User> pagingUser) {
        for (int i = 0; i < 10; i++) {
            pagingUser.add(allUser.get(i));
        }
    }

    @Test
    @DisplayName("ReviewResponse List 반환")
    void returnReviewResponse() {
        List<User> allUser = userFactory.createGuests();
        List<User> filterUser = new ArrayList<>();
        pagingUser(allUser, filterUser);
        ReviewResponse reviewResponse = new ReviewResponse(
                "test", "testNum", "testNickname", new ArrayList<>());
        given(modelMapper.map(any(), any())).willReturn(reviewResponse);

        List<ReviewResponse> reviewResponses = adminService.returnReviewResponse(filterUser);

        assertEquals(10, reviewResponses.size());
    }

    @Test
    @DisplayName("Guest에서 User로 변경")
    void convertToUser() {
        User guest = userFactory.createGuest("test", "testNickname");
        assertTrue(guest.isSort());
        assertEquals(RoleType.GUEST, guest.getRoleType());

        adminService.convertToUser(guest);

        assertFalse(guest.isSort());
        assertEquals(RoleType.USER, guest.getRoleType());
    }
}