package com.voilandPantry.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.voilandPantry.models.Student;
import com.voilandPantry.repositories.StudentRepository;

@Controller
public class LoginController {

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String identifier,
                               Model model) {

        // Debug logging
        System.out.println("DEBUG: Login attempt with identifier = " + identifier);

        // Find the student in MySQL
        Optional<Student> studentOpt = studentRepository.findByIdentifier(identifier);

        if (studentOpt.isPresent()) {
            // REDIRECT to the welcome URL so VisitController can save the visit to MySQL
            String redirectUrl = "redirect:/welcome?studentId=" + studentOpt.get().getId();
            System.out.println("DEBUG: Student found. Redirecting to: " + redirectUrl);
            return redirectUrl;
        } else {
            // Student not found, send to registration
            System.out.println("DEBUG: Student not found. Sending to registration.");
            model.addAttribute("identifier", identifier);
            return "register_student";
        }
    }

    @PostMapping("/register")
    public String registerStudent(@RequestParam String identifier,
                                  @RequestParam String year,
                                  @RequestParam String major,
                                  Model model) {
        // Debug logging
        System.out.println("DEBUG: Register attempt with identifier = " + identifier);
        System.out.println("DEBUG: Year = " + year + ", Major = " + major);
        
        // 1. Create and save the new student
        Student newStudent = new Student(identifier, year, major);
        newStudent = studentRepository.save(newStudent); // Capture the saved student to get the ID
        
        System.out.println("DEBUG: New student registered with ID = " + newStudent.getId());

        // 2. REDIRECT to the welcome URL with the new ID
        // This triggers the VisitController to start a new visit record
        return "redirect:/welcome?studentId=" + newStudent.getId();
    }
}
