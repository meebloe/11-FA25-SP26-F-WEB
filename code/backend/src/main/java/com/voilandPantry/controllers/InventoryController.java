package com.voilandPantry.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.voilandPantry.models.Inventory;
import com.voilandPantry.repositories.InventoryRepository;

@Controller
public class InventoryController {

    private final InventoryRepository inventoryRepository;

    public InventoryController(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    // =========================
    // HELPER METHOD (NEW)
    // =========================
    private boolean isAuthorized(HttpSession session) {
        // Check admin (Spring Security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.isAuthenticated() &&
                auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Check volunteer (session)
        Long volunteerId = (Long) session.getAttribute("volunteerId");

        return isAdmin || volunteerId != null;
    }

    // =========================
    // VIEW INVENTORY
    // =========================
    @GetMapping("/inventory")
    public String viewInventory(@RequestParam(required = false) String search,
                                Model model,
                                HttpSession session) {

        if (!isAuthorized(session)) {
            return "redirect:/login"; // 🚀 redirect students away
        }

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

    // =========================
    // ADD ITEM
    // =========================
    @PostMapping("/inventory/add")
    public String addItem(@ModelAttribute Inventory item, HttpSession session) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        inventoryRepository.save(item);
        return "redirect:/inventory";
    }

    // =========================
    // DELETE ITEM
    // =========================
    @GetMapping("/inventory/delete/{id}")
    public String deleteItem(@PathVariable Long id, HttpSession session) {
        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        inventoryRepository.deleteById(id);
        return "redirect:/inventory";
    }

    // =========================
    // UPDATE ITEM
    // =========================
    @PostMapping("/inventory/update/{id}")
    public String updateItem(@PathVariable Long id,
                             @ModelAttribute Inventory updatedItem,
                             HttpSession session) {

        if (!isAuthorized(session)) {
            return "redirect:/login";
        }

        updatedItem.setId(id);
        inventoryRepository.save(updatedItem);
        return "redirect:/inventory";
    }

    // =========================
    // JSON UPDATE
    // =========================
    @PostMapping(path = "/inventory/{id}", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateInventoryJson(@PathVariable Long id,
                                                                   @RequestBody Inventory updatedItem,
                                                                   HttpSession session) {

        if (!isAuthorized(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "unauthorized"));
        }

        Optional<Inventory> optional = inventoryRepository.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "not_found", "message", "Inventory item not found"));
        }

        Inventory item = optional.get();
        item.setUpc(updatedItem.getUpc());
        item.setProductName(updatedItem.getProductName());
        item.setNetWeight(updatedItem.getNetWeight());
        item.setQuantity(updatedItem.getQuantity());

        inventoryRepository.save(item);

        return ResponseEntity.ok(Map.of("status", "ok", "message", "Item updated successfully"));
    }

    // =========================
    // CHECKOUT
    // =========================
    @PostMapping(path = "/inventory/checkout", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody Map<String, String> payload) {

        String code = payload.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Missing code"));
        }

        Optional<Inventory> optional = inventoryRepository.findByUpc(code.trim());
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "not_found"));
        }

        Inventory item = optional.get();
        if (item.getQuantity() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "out_of_stock"));
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

    // =========================
    // BARCODE CHECK
    // =========================
    @PostMapping(path = "/inventory/barcodecheck", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> barcodecheck(@RequestBody Map<String, String> payload) {

        String code = payload.get("code");
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Missing code"));
        }

        Optional<Inventory> optional = inventoryRepository.findByUpc(code.trim());
        if (optional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("status", "not_found"));
        }

        Inventory item = optional.get();

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "upc", item.getUpc(),
                "productName", item.getProductName(),
                "quantity", item.getQuantity()
        ));
    }

    // =========================
    // SCAN IN
    // =========================
    @PostMapping(path = "/inventory/scanin", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> scanIn(@RequestBody Map<String, Object> payload,
                                                      HttpSession session) {

        if (!isAuthorized(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "unauthorized"));
        }

        String code = (payload.get("code") != null) ? payload.get("code").toString().trim() : null;
        if (code == null || code.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "error", "message", "Missing code"));
        }

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

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", "not_found"));
    }
}