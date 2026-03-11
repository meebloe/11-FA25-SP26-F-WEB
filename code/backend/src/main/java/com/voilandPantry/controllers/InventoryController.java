package com.voilandPantry.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.voilandPantry.models.Inventory;
import com.voilandPantry.repositories.InventoryRepository;

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

    // Update endpoint for JSON requests: accepts JSON with inventory fields
    @PostMapping(path = "/inventory/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateInventoryJson(@PathVariable Long id, @RequestBody Inventory updatedItem) {
        Optional<Inventory> optional = inventoryRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "not_found", "message", "Inventory item not found"));
        }
        Inventory item = optional.get();
        item.setUpc(updatedItem.getUpc());
        item.setProductName(updatedItem.getProductName());
        item.setNetWeight(updatedItem.getNetWeight());
        item.setQuantity(updatedItem.getQuantity());
        inventoryRepository.save(item);
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Item updated successfully"));
    }

    @PostMapping(path = "/inventory/checkout", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Missing code"));
        }
        Optional<Inventory> optional = inventoryRepository.findByUpc(code.trim());
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "not_found"));
        }
        Inventory item = optional.get();
        if (item.getQuantity() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "out_of_stock"));
        }
        item.setQuantity(item.getQuantity() - 1);
        inventoryRepository.save(item);
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "upc", item.getUpc(),
                "productName", item.getProductName(),
                "newQuantity", item.getQuantity()
        ));
    }

    // Barcodecheck endpoint: checks if UPC exists, returns 200 or 404
    @PostMapping(path = "/inventory/barcodecheck", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> barcodecheck(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Missing code"));
        }
        Optional<Inventory> optional = inventoryRepository.findByUpc(code.trim());
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "not_found"));
        }
        Inventory item = optional.get();
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "upc", item.getUpc(),
                "productName", item.getProductName(),
                "quantity", item.getQuantity()
        ));
    }

    // Scan-in endpoint: accepts JSON { "code": "<UPC>", "quantity": 5 } for existing items
    // OR { "code": "<UPC>", "productName": "...", "netWeight": 12.5, "quantity": 5 } for new items
    @PostMapping(path = "/inventory/scanin", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> scanIn(@RequestBody Map<String, Object> payload) {
        String code = (payload.get("code") != null) ? payload.get("code").toString().trim() : null;
        if (code == null || code.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", "error", "message", "Missing code"));
        }
        
        // Parse quantity from payload
        int quantityToAdd = 1;
        if (payload.get("quantity") != null) {
            try {
                quantityToAdd = Integer.parseInt(payload.get("quantity").toString());
            } catch (NumberFormatException e) {
                quantityToAdd = 1;
            }
        }
        
        Optional<Inventory> optional = inventoryRepository.findByUpc(code);
        if (optional.isPresent()) {
            Inventory item = optional.get();
            item.setQuantity(item.getQuantity() + quantityToAdd);
            inventoryRepository.save(item);
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "upc", item.getUpc(),
                    "productName", item.getProductName(),
                    "newQuantity", item.getQuantity()
            ));
        }

        // not found: check if payload contains productName and netWeight to create new record
        Object nameObj = payload.get("productName");
        Object netObj = payload.get("netWeight");
        if (nameObj != null && netObj != null) {
            String name = nameObj.toString();
            double netWeight;
            try {
                netWeight = Double.parseDouble(netObj.toString());
            } catch (NumberFormatException e) {
                netWeight = 0.0;
            }
            Inventory newItem = new Inventory(code, name, netWeight, quantityToAdd);
            inventoryRepository.save(newItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "status", "created",
                    "upc", newItem.getUpc(),
                    "productName", newItem.getProductName(),
                    "newQuantity", newItem.getQuantity()
            ));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", "not_found"));
    }
}