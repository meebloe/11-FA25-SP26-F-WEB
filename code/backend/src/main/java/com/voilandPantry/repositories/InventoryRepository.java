package com.voilandPantry.repositories;

import com.voilandPantry.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProductNameContainingIgnoreCase(String keyword);
    Optional<Inventory> findByUpc(String upc);
}