package com.voilandPantry.controllers;

import com.voilandPantry.models.Volunteer;
import com.voilandPantry.models.VolunteerHours;
import com.voilandPantry.repositories.VolunteerHoursRepository;
import com.voilandPantry.repositories.VolunteerRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/volunteer")
public class VolunteerHoursController {

    @Autowired
    private VolunteerRepository volunteerRepository;

    @Autowired
    private VolunteerHoursRepository hoursRepository;

    // =========================
    // SHOW FORM
    // =========================
    @GetMapping("/hours")
    public String showHoursForm(HttpSession session, Model model) {
        Long volunteerId = (Long) session.getAttribute("volunteerId");

        if (volunteerId == null) {
            return "redirect:/volunteer/login";
        }

        model.addAttribute("volunteerName", session.getAttribute("volunteerName"));
        return "volunteer_hours";
    }

    // =========================
    // SUBMIT HOURS
    // =========================
    @PostMapping("/hours")
    public String submitHours(@RequestParam double hours,
                             @RequestParam String date,
                             HttpSession session,
                             Model model) {

        Long volunteerId = (Long) session.getAttribute("volunteerId");

        if (volunteerId == null) {
            return "redirect:/volunteer/login";
        }

        Optional<Volunteer> volunteerOpt = volunteerRepository.findById(volunteerId);

        if (volunteerOpt.isEmpty()) {
            return "redirect:/volunteer/login";
        }

        VolunteerHours entry = new VolunteerHours(
                hours,
                date,
                volunteerOpt.get()
        );

        hoursRepository.save(entry);

        model.addAttribute("message", "Hours submitted successfully!");
        return "volunteer_hours";
    }
}