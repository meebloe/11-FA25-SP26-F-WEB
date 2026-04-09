package com.voilandPantry.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.voilandPantry.models.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAll();
    
    default Optional<Student> findByHashedIdentifier(String plainIdentifier) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        List<Student> students = findAll();
        return students.stream()
                .filter(student -> encoder.matches(plainIdentifier, student.getIdentifier()))
                .findFirst();
    }
}
