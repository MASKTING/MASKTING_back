package com.maskting.backend.controller;

import com.maskting.backend.domain.User;
import com.maskting.backend.dto.request.ReviewRequest;
import com.maskting.backend.dto.request.SignUpRejectRequest;
import com.maskting.backend.dto.response.DataTableResponse;
import com.maskting.backend.dto.response.ReviewResponse;
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
    private final AdminService adminService;

    @GetMapping
    String home(HttpServletRequest request, Model model) {
//        model.addAttribute("name", getAdminName(request));
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
        return new DataTableResponse(getDraw(reviewRequest),
                adminService.getTotal(name),
                adminService.getTotal(name),
                getReviewResponses(getGuests(reviewRequest, name)));
    }

    private List<User> getGuests(ReviewRequest reviewRequest, String name) {
        return isSearching(name) ? adminService.findSortingUserByName(reviewRequest, name) : adminService.findSortingUser(reviewRequest);
    }

    private int getDraw(ReviewRequest reviewRequest) {
        return reviewRequest.getDraw();
    }

    private List<ReviewResponse> getReviewResponses(List<User> guests) {
        return adminService.returnReviewResponse(guests);
    }

    private boolean isSearching(String name) {
        return name.length() > 0;
    }

    @PostMapping("/reject/{nickname}")
    String reject(@PathVariable String nickname, SignUpRejectRequest request){
        adminService.rejectSignUp(adminService.getUserByNickName(nickname), request.getReason());
        return "redirect:/admin";
    }

    @PostMapping("/approval/{nickname}")
    String approval(@PathVariable String nickname){
        adminService.convertToUser(adminService.getUserByNickName(nickname));
        return "redirect:/admin";
    }

}
