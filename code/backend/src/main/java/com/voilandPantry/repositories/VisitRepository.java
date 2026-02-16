package com.voilandPantry.repositories;

import com.voilandPantry.models.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    // Visits per major
    @Query("""
        SELECT v.student.major, COUNT(v)
        FROM Visit v
        GROUP BY v.student.major
    """)
    List<Object[]> visitsByMajor();

    // Items by major
    @Query("""
        SELECT v.student.major, v.items, COUNT(v)
        FROM Visit v
        GROUP BY v.student.major, v.items
    """)
    List<Object[]> itemsByMajor();

    // Items per month
    @Query("""
        SELECT MONTH(v.timestamp), v.items, COUNT(v)
        FROM Visit v
        GROUP BY MONTH(v.timestamp), v.items
    """)
    List<Object[]> itemsPerMonth();

}