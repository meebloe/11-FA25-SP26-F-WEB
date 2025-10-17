package com.voilandPantry.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VolunteerFormController {

    @GetMapping("/volunteer-form")
    public String volunteerFormPage() {
        return "volunteer_form"; // maps to volunteer_form.html in templates
    }
}