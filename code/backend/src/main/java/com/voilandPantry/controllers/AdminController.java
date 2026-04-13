package com.voilandPantry.controllers;

import com.voilandPantry.models.AdminUser;
import com.voilandPantry.repositories.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/manage-admins")
    public String manageAdmins(Model model) {
        model.addAttribute("admins", adminUserRepository.findAll());
        return "manage-admins"; // must match manage-admins.html
    }

    @PostMapping("/delete")
    public String deleteAdmin(@RequestParam String username, RedirectAttributes redirectAttributes) {
        var admin = adminUserRepository.findByUsername(username);

        if (admin.isPresent()) {
            adminUserRepository.delete(admin.get());
            redirectAttributes.addFlashAttribute("success", "Admin deleted.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Admin not found.");
        }

        return "redirect:/admin/manage-admins";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String username,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {

        var adminOpt = adminUserRepository.findByUsername(username);

        if (adminOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Admin user not found!");
        } else {
            AdminUser admin = adminOpt.get();
            admin.setPasswordHash(passwordEncoder.encode(newPassword));
            adminUserRepository.save(admin);
            redirectAttributes.addFlashAttribute("success", "Password reset successfully!");
        }

        return "redirect:/admin/manage-admins";
    }

}