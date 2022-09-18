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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final AdminService adminService;

    @GetMapping
    String home(HttpServletRequest request, Model model) {
        model.addAttribute("name", getAdminName(request));
        return "admin/home";
    }

    private String getAdminName(HttpServletRequest request) {
        return getAdmin(request).getName();
    }

    private User getAdmin(HttpServletRequest request) {
        return adminService.getAdmin(request);
    }

    @ResponseBody
    @GetMapping("/guests")
    DataTableResponse returnGuests(ReviewRequest reviewRequest, HttpServletRequest request) {
        String name = request.getParameter("search[value]");
        int total = 0;
        List<User> guests = new ArrayList<>();

        if(isSearching(name)){
            total = userRepository.countByNameContains(name);
            guests = adminService.findSortingUserByName(reviewRequest, name);
        }else{
            total = (int)userRepository.count();
            guests = adminService.findSortingUser(reviewRequest);
        }

        List<ReviewResponse> reviewResponses = adminService.returnReviewResponse(guests);;
        int draw = reviewRequest.getDraw();
        return new DataTableResponse(draw, total, total, reviewResponses);
    }

    private boolean isSearching(String name) {
        return name.length() > 0;
    }

    @PostMapping("/approval/{name}")
    String approval(@PathVariable String name){
        User user = userRepository.findByName(name);
        adminService.convertToUser(user);
        return "admin/home";
    }
}
