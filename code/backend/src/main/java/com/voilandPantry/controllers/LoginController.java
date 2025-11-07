package com.voilandPantry.controllers;

import com.voilandPantry.models.Student;
import com.voilandPantry.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String firstName,
                               @RequestParam String lastName,
                               Model model) {

        var student = studentRepository.findByFirstNameAndLastName(firstName, lastName);

        if (student.isPresent()) {
            model.addAttribute("message", "Welcome back, " + firstName + "!");
            return "welcome";
        } else {
            model.addAttribute("firstName", firstName);
            model.addAttribute("lastName", lastName);
            return "register_student";
        }
    }

    @PostMapping("/register")
    public String registerStudent(@RequestParam String firstName,
                                  @RequestParam String lastName,
                                  @RequestParam String year,
                                  @RequestParam String major,
                                  Model model) {
        Student newStudent = new Student(firstName, lastName, year, major);
        studentRepository.save(newStudent);

        model.addAttribute("message", "Welcome, " + firstName + "! You’ve been registered.");
        return "welcome";
    }
}