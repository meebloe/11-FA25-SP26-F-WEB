package com.voilandPantry.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {

    @GetMapping("/admin-login")
    public String showLoginPage() {
        return "admin_login"; // Thymeleaf template
    }
}