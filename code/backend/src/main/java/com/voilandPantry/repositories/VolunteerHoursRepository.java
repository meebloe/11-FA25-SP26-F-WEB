package com.voilandPantry.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voilandPantry.models.Volunteer;
import com.voilandPantry.models.VolunteerHours;

public interface VolunteerHoursRepository extends JpaRepository<VolunteerHours, Long> {
    List<VolunteerHours> findByVolunteer(Volunteer volunteer);
}