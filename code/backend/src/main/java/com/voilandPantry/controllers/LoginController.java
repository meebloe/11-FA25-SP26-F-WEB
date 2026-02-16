package com.voilandPantry.controllers;

import com.voilandPantry.models.Student;
import com.voilandPantry.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

        // Find the student in MySQL
        Optional<Student> studentOpt = studentRepository.findByFirstNameAndLastName(firstName, lastName);

        if (studentOpt.isPresent()) {
            // REDIRECT to the welcome URL so VisitController can save the visit to MySQL
            return "redirect:/welcome?studentId=" + studentOpt.get().getId();
        } else {
            // Student not found, send to registration
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
        // 1. Create and save the new student
        Student newStudent = new Student(firstName, lastName, year, major);
        newStudent = studentRepository.save(newStudent); // Capture the saved student to get the ID

        // 2. REDIRECT to the welcome URL with the new ID
        // This triggers the VisitController to start a new visit record
        return "redirect:/welcome?studentId=" + newStudent.getId();
    }
}
