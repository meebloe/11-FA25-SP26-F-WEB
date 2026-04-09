package com.voilandPantry.controllers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Hash identifier using SHA-256 to handle long card swipe data.
     * BCrypt has a 72-byte limit, so we pre-hash with SHA-256 first.
     */
    private String hashIdentifierWithSHA256(String identifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(identifier.getBytes());
            return toHexString(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private String toHexString(byte[] bytes) {
        try (Formatter formatter = new Formatter()) {
            for (byte b : bytes) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String identifier,
                               Model model) {

        // Debug logging
        System.out.println("DEBUG: Login attempt with identifier = " + identifier);

        // Hash the identifier with SHA-256 first to normalize it
        String normalizedIdentifier = hashIdentifierWithSHA256(identifier);
        
        // Find the student in MySQL by comparing hashed identifiers
        Optional<Student> studentOpt = studentRepository.findByHashedIdentifier(normalizedIdentifier);

        if (studentOpt.isPresent()) {
            // REDIRECT to the welcome URL so VisitController can save the visit to MySQL
            String redirectUrl = "redirect:/welcome?studentId=" + studentOpt.get().getId();
            System.out.println("DEBUG: Student found. Redirecting to: " + redirectUrl);
            return redirectUrl;
        } else {
            // Student not found, send to registration
            System.out.println("DEBUG: Student not found. Sending to registration.");
            model.addAttribute("identifier", normalizedIdentifier);
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
        
        // 1. Hash the identifier with SHA-256 (deterministic hash for card identifiers)
        Student newStudent = new Student(identifier, year, major);
        newStudent = studentRepository.save(newStudent); // Capture the saved student to get the ID
        
        System.out.println("DEBUG: New student registered with ID = " + newStudent.getId());

        // 2. REDIRECT to the welcome URL with the new ID
        // This triggers the VisitController to start a new visit record
        return "redirect:/welcome?studentId=" + newStudent.getId();
    }

    @PostMapping("/guest-login")
    public String registerGuestStudent(@RequestParam String year,
                                       @RequestParam String major,
                                       Model model) {
        // Debug logging
        System.out.println("DEBUG: Guest login attempt with year = " + year + ", major = " + major);
        
        // 1. Generate a trivial identifier (GUEST_ + UUID)
        String trivialIdentifier = "GUEST_" + UUID.randomUUID().toString();
        System.out.println("DEBUG: Generated trivial identifier = " + trivialIdentifier);
        
        // 2. Hash the identifier with SHA-256 (deterministic hash for card identifiers)
        String hashedIdentifier = hashIdentifierWithSHA256(trivialIdentifier);
        Student guestStudent = new Student(hashedIdentifier, year, major);
        guestStudent = studentRepository.save(guestStudent); // Capture the saved student to get the ID
        
        System.out.println("DEBUG: Guest student registered with ID = " + guestStudent.getId());

        // 3. REDIRECT to the welcome URL with the new ID
        return "redirect:/welcome?studentId=" + guestStudent.getId();
    }
}
