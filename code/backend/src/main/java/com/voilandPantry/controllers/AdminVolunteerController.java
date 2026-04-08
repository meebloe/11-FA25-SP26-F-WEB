package com.voilandPantry.controllers;

import com.voilandPantry.models.Volunteer;
import com.voilandPantry.repositories.VolunteerRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/volunteers")
public class AdminVolunteerController {

    private final VolunteerRepository volunteerRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminVolunteerController(VolunteerRepository volunteerRepository,
                                    PasswordEncoder passwordEncoder) {
        this.volunteerRepository = volunteerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================
    // VIEW PAGE
    // =========================
    @GetMapping
    public String viewVolunteers(Model model) {
        List<Volunteer> volunteers = volunteerRepository.findAll();
        model.addAttribute("volunteers", volunteers);
        return "admin_volunteers";
    }

    // =========================
    // RESET PASSWORD
    // =========================
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam Long id,
                                @RequestParam String newPassword) {

        Volunteer volunteer = volunteerRepository.findById(id).orElse(null);

        if (volunteer != null) {
            volunteer.setPasswordHash(passwordEncoder.encode(newPassword));
            volunteerRepository.save(volunteer);
        }

        return "redirect:/admin/volunteers";
    }

    // =========================
    // DELETE VOLUNTEER
    // =========================
    @PostMapping("/delete")
    public String deleteVolunteer(@RequestParam Long id) {
        volunteerRepository.deleteById(id);
        return "redirect:/admin/volunteers";
    }
}