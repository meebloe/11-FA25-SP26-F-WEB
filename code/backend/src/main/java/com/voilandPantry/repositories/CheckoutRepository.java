package com.voilandPantry.repositories;

import com.voilandPantry.models.Checkout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CheckoutRepository extends JpaRepository<Checkout, Long> {
    List<Checkout> findByStudentId(Long studentId);
    List<Checkout> findByProductId(Long productId);
    List<Checkout> findByCheckoutTimeBetween(LocalDateTime start, LocalDateTime end);
}