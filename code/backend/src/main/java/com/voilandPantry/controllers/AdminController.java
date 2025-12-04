package com.voilandPantry.controllers;

import com.voilandPantry.models.AdminUser;
import com.voilandPantry.repositories.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Show dashboard with admin creation form
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("admins", adminUserRepository.findAll());
        return "admin_dashboard";
    }

    // Handle new admin creation
    @PostMapping("/add")
    public String addAdmin(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {

        // Check if username already exists
        if (adminUserRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists!");
        } else {
            String hashedPassword = passwordEncoder.encode(password);
            AdminUser newAdmin = new AdminUser(username, hashedPassword);
            adminUserRepository.save(newAdmin);
            model.addAttribute("success", "New admin added!");
        }

        model.addAttribute("admins", adminUserRepository.findAll());
        return "admin_dashboard";
    }
}