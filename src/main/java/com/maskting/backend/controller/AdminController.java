package com.maskting.backend.controller;

import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.ReviewRequest;
import com.maskting.backend.dto.response.DataTableResponse;
import com.maskting.backend.dto.response.ReviewResponse;
import com.maskting.backend.repository.UserRepository;
import com.maskting.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final AdminService adminService;

    @GetMapping
    String home(HttpServletRequest request, Model model) {
        User admin = adminService.getUser(request);
        model.addAttribute("name", admin.getName());

        return "admin/home";
    }

    @ResponseBody
    @GetMapping("/guests")
    DataTableResponse returnGuests(ReviewRequest reviewRequest) {
        List<User> guests = adminService.findSortingUser(reviewRequest);
        List<ReviewResponse> reviewResponses = adminService.returnReviewResponse(guests);
        int draw = reviewRequest.getDraw();
        int total = (int)userRepository.count();
        return new DataTableResponse(draw, total, total, reviewResponses);
    }

}
