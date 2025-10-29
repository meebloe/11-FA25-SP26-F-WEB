package com.voilandPantry.controllers;

import com.voilandPantry.models.Checkout;
import com.voilandPantry.models.Inventory;
import com.voilandPantry.repositories.CheckoutRepository;
import com.voilandPantry.repositories.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/checkouts")
public class CheckoutController {

    private final CheckoutRepository checkoutRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public CheckoutController(CheckoutRepository checkoutRepository, InventoryRepository inventoryRepository) {
        this.checkoutRepository = checkoutRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // Create a new checkout transaction
    @PostMapping
    public ResponseEntity<?> createCheckout(@RequestBody Checkout checkout) {
        // Validate that product and student exist (you might want to add StudentRepository validation too)
        if (!inventoryRepository.existsById(checkout.getProductId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Product not found"));
        }
        
        checkout.setCheckoutTime(LocalDateTime.now());
        Checkout saved = checkoutRepository.save(checkout);
        return ResponseEntity.ok(saved);
    }

    // Get all checkouts
    @GetMapping
    public List<Checkout> getAllCheckouts() {
        return checkoutRepository.findAll();
    }

    // Get checkouts by student ID
    @GetMapping("/student/{studentId}")
    public List<Checkout> getCheckoutsByStudent(@PathVariable Long studentId) {
        return checkoutRepository.findByStudentId(studentId);
    }

    // Get checkouts by product ID
    @GetMapping("/product/{productId}")
    public List<Checkout> getCheckoutsByProduct(@PathVariable Long productId) {
        return checkoutRepository.findByProductId(productId);
    }

    // Delete a specific checkout
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCheckout(@PathVariable Long id) {
        if (!checkoutRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        checkoutRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Calculate total weight of all checked out items
    @GetMapping("/weight-total")
    public ResponseEntity<?> getTotalWeight() {
        List<Checkout> checkouts = checkoutRepository.findAll();
        double totalWeight = 0.0;
        
        for (Checkout checkout : checkouts) {
            Optional<Inventory> inventoryItem = inventoryRepository.findById(checkout.getProductId());
            if (inventoryItem.isPresent()) {
                totalWeight += inventoryItem.get().getNetWeight();
            }
        }
        
        return ResponseEntity.ok(Map.of(
            "totalWeight", totalWeight,
            "unit", "oz"
        ));
    }

    // Clear all checkouts
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearAllCheckouts() {
        checkoutRepository.deleteAll();
        return ResponseEntity.ok(Map.of(
            "message", "All checkouts have been cleared",
            "timestamp", LocalDateTime.now()
        ));
    }

    // Get checkouts within a date range
    @GetMapping("/date-range")
    public List<Checkout> getCheckoutsByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return checkoutRepository.findByCheckoutTimeBetween(start, end);
    }
}