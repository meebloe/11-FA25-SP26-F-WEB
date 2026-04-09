package com.voilandPantry.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.voilandPantry.models.Volunteer;
import com.voilandPantry.repositories.VolunteerRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/volunteer")
public class VolunteerAuthController {

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================
    // LOGIN PAGE
    // =========================
    @GetMapping("/login")
    public String showLogin() {
        return "volunteer_login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {

        Optional<Volunteer> volunteerOpt = volunteerRepository.findByEmail(email);

        if (volunteerOpt.isPresent()) {
            Volunteer volunteer = volunteerOpt.get();

            if (passwordEncoder.matches(password, volunteer.getPasswordHash())) {
                session.setAttribute("volunteerId", volunteer.getId());
                session.setAttribute("volunteerName", volunteer.getName());
                return "redirect:/volunteer/dashboard";
            }
        }

        model.addAttribute("error", "Invalid email or password");
        return "volunteer_login";
    }

    // =========================
    // REGISTER PAGE
    // =========================
    @GetMapping("/register")
    public String showRegister() {
        return "volunteer_register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String email,
                                 @RequestParam String name,
                                 @RequestParam String password,
                                 Model model) {

        if (volunteerRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already exists");
            return "volunteer_register";
        }

        Volunteer volunteer = new Volunteer(
                email,
                passwordEncoder.encode(password),
                name
        );

        volunteerRepository.save(volunteer);

        return "redirect:/volunteer/login";
    }

    // =========================
    // DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String name = (String) session.getAttribute("volunteerName");

        if (name == null) {
            return "redirect:/volunteer/login";
        }

        model.addAttribute("name", name);
        return "volunteer_dashboard";
    }

    // =========================
    // LOGOUT
    // =========================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}