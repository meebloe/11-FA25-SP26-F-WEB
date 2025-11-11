package com.voilandPantry.repositories;

import com.voilandPantry.models.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Long> {
}
