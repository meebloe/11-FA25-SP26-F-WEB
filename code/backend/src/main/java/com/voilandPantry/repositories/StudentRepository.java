package com.voilandPantry.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.voilandPantry.models.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAll();
    
    /**
     * Find a student by their SHA-256 hashed identifier.
     * Uses direct string comparison since SHA-256 is deterministic.
     */
    default Optional<Student> findByHashedIdentifier(String hashedIdentifier) {
        List<Student> students = findAll();
        System.out.println("DEBUG (StudentRepository): Looking for hashed identifier = " + hashedIdentifier);
        System.out.println("DEBUG (StudentRepository): Total students in database = " + students.size());
        
        Optional<Student> result = students.stream()
                .filter(student -> {
                    boolean matches = hashedIdentifier.equals(student.getIdentifier());
                    System.out.println("DEBUG (StudentRepository): Comparing " + hashedIdentifier + " with " + student.getIdentifier() + " = " + matches);
                    return matches;
                })
                .findFirst();
        
        System.out.println("DEBUG (StudentRepository): Result found = " + result.isPresent());
        return result;
    }
}
