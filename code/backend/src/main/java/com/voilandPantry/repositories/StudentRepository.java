package com.voilandPantry.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.voilandPantry.models.Student;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByFirstNameAndLastName(String firstName, String lastName);
}
