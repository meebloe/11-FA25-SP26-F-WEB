package com.voilandPantry.controllers;

import com.voilandPantry.models.Volunteer;
import com.voilandPantry.repositories.VolunteerRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
    @Autowired
    private com.voilandPantry.repositories.VolunteerHoursRepository hoursRepository; // Add this line at the top with other Autowired fields

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long volunteerId = (Long) session.getAttribute("volunteerId");
        String name = (String) session.getAttribute("volunteerName");

        if (volunteerId == null) {
            return "redirect:/volunteer/login";
        }

        // 1. Find the volunteer object
        Optional<Volunteer> volunteerOpt = volunteerRepository.findById(volunteerId);
        
        if (volunteerOpt.isPresent()) {
            Volunteer v = volunteerOpt.get();
            
            // 2. Fetch the hours for this specific volunteer
            java.util.List<com.voilandPantry.models.VolunteerHours> allHours = hoursRepository.findByVolunteer(v);

            // 3. Calculate the total sum of hours
            double total = allHours.stream()
                                .mapToDouble(com.voilandPantry.models.VolunteerHours::getHours)
                                .sum();

            // 4. Send everything to the dashboard
            model.addAttribute("name", name);
            model.addAttribute("totalHours", total);
            model.addAttribute("recentHours", allHours); 
        }

        return "volunteer_dashboard";
    }

    // =========================
    // LOGOUT
    // =========================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/volunteer/login";
    }
}