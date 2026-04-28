package com.voilandPantry.models;

import jakarta.persistence.*;

@Entity
public class VolunteerHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double hours;

    private String date;

    @ManyToOne
    @JoinColumn(name = "volunteer_id") // Ensures it matches the foreign key column
    private Volunteer volunteer;

    public VolunteerHours() {}

    public VolunteerHours(double hours, String date, Volunteer volunteer) {
        this.hours = hours;
        this.date = date;
        this.volunteer = volunteer;
    }

    public Long getId() { return id; }

    public double getHours() { return hours; }
    public void setHours(double hours) { this.hours = hours; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Volunteer getVolunteer() { return volunteer; }
    public void setVolunteer(Volunteer volunteer) { this.volunteer = volunteer; }
}