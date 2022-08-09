package com.maskting.backend.controller;

import com.maskting.backend.domain.User;
import com.maskting.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    String home(HttpServletRequest request, Model model) {
        User user = adminService.getUser(request);
        model.addAttribute("name", user.getName());

        return "admin/home";
    }

}
