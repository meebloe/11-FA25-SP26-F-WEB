package com.voilandPantry.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voilandPantry.models.Volunteer;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    Optional<Volunteer> findByEmail(String email);
}