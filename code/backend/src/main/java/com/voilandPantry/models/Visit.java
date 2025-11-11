package com.voilandPantry.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private String items; // comma-separated list or summary of items
    private LocalDateTime timestamp;

    public Visit() {}

    public Visit(Student student, String items) {
        this.student = student;
        this.items = items;
        this.timestamp = LocalDateTime.now();
    }

    // Getters & setters
    public Long getId() { return id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
