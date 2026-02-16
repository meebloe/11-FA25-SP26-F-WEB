package com.voilandPantry.controllers;

import com.voilandPantry.models.Student;
import com.voilandPantry.models.Visit;
import com.voilandPantry.repositories.StudentRepository;
import com.voilandPantry.repositories.VisitRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@Controller
public class VisitController {

    private final VisitRepository visitRepository;
    private final StudentRepository studentRepository;

    public VisitController(VisitRepository visitRepository,
                           StudentRepository studentRepository) {
        this.visitRepository = visitRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    @GetMapping("/welcome") 
    public String welcomeStudent(@RequestParam Long studentId, HttpSession session, Model model) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found: " + studentId));

        if (session.getAttribute("visitId") == null) {
            // Create the visit and ensure the student object is attached
            Visit visit = new Visit(student, "");
            
            // Explicitly set the timestamp here just in case
            visit.setTimestamp(java.time.LocalDateTime.now());
            
            // Save and FLUSH to force MySQL to write the row immediately
            visit = visitRepository.saveAndFlush(visit); 
            
            session.setAttribute("visitId", visit.getId());
            System.out.println("DEBUG: Visit saved with ID: " + visit.getId());
        }

        model.addAttribute("student", student);
        return "welcome";
    }

    // Complete the visit
    @PostMapping("/end-visit")
    public String endVisit(HttpSession session, @RequestParam(required = false) String items) {
        Long visitId = (Long) session.getAttribute("visitId");
        if (visitId != null) {
            Visit visit = visitRepository.findById(visitId).orElse(null);
            if (visit != null && items != null) {
                visit.setItems(items.trim());
                visitRepository.save(visit);
            }
            session.removeAttribute("visitId");
        }
        return "redirect:/"; // You can create a thank-you page
    }
}