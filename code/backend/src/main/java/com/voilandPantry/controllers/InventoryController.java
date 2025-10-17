package com.voilandPantry.controllers;

import com.voilandPantry.models.Inventory;
import com.voilandPantry.repositories.InventoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    public InventoryController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @GetMapping("/inventory")
    public String viewInventory(@RequestParam(required = false) String search, Model model) {
        List<Inventory> items;
        if (search != null && !search.isEmpty()) {
            items = inventoryRepository.findByProductNameContainingIgnoreCase(search);
        } else {
            items = inventoryRepository.findAll();
        }
        model.addAttribute("inventory", items);
        model.addAttribute("search", search);
        return "inventory";
    }

    @PostMapping("/inventory/add")
    public String addItem(@ModelAttribute Inventory item) {
        inventoryRepository.save(item);
        return "redirect:/inventory";
    }

    @GetMapping("/inventory/delete/{id}")
    public String deleteItem(@PathVariable Long id) {
        inventoryRepository.deleteById(id);
        return "redirect:/inventory";
    }

    @PostMapping("/inventory/update/{id}")
    public String updateItem(@PathVariable Long id, @ModelAttribute Inventory updatedItem) {
        updatedItem.setId(id);
        inventoryRepository.save(updatedItem);
        return "redirect:/inventory";
    }
}