package com.voilandPantry.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.voilandPantry.models.Inventory;
import com.voilandPantry.models.Student;
import com.voilandPantry.models.Visit;
import com.voilandPantry.repositories.InventoryRepository;
import com.voilandPantry.repositories.VisitRepository;
import com.voilandPantry.services.ReportPdfService;

@Controller
public class ReportController {

    private final InventoryRepository inventoryRepository;
    private final VisitRepository visitRepository;
    private final ReportPdfService pdfService;

    public ReportController(InventoryRepository inventoryRepository,
                            VisitRepository visitRepository,
                            ReportPdfService pdfService) {
        this.inventoryRepository = inventoryRepository;
        this.visitRepository = visitRepository;
        this.pdfService = pdfService;
    }

    /**
     * Compute most popular item per major, filtered by date range.
     */
    private List<Object[]> computeItemsByMajor(List<Visit> filteredVisits) {
        Map<String, Map<String, Integer>> majorItemCounts = new HashMap<>();

        for (Visit visit : filteredVisits) {
            Student student = visit.getStudent();
            if (student == null || visit.getItems() == null || visit.getItems().isEmpty()) continue;

            String major = student.getMajor();
            majorItemCounts.putIfAbsent(major, new HashMap<>());
            Map<String, Integer> itemCounts = majorItemCounts.get(major);

            String[] items = visit.getItems().split(",");
            for (String item : items) {
                item = item.trim();
                if (!item.isEmpty()) {
                    itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
                }
            }
        }

        // For each major, find the most popular item
        List<Object[]> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : majorItemCounts.entrySet()) {
            String major = entry.getKey();
            Map<String, Integer> counts = entry.getValue();

            String popularItem = counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            int frequency = counts.getOrDefault(popularItem, 0);

            result.add(new Object[]{major, popularItem, frequency});
        }

        return result;
    }

    /**
     * Compute item counts per month, filtered by date range.
     */
    private List<Object[]> computeItemsPerMonth(List<Visit> filteredVisits) {
        Map<Integer, Map<String, Integer>> monthItemCounts = new HashMap<>();

        for (Visit visit : filteredVisits) {
            if (visit.getItems() == null || visit.getItems().isEmpty() || visit.getTimestamp() == null) continue;

            int month = visit.getTimestamp().getMonthValue();
            monthItemCounts.putIfAbsent(month, new HashMap<>());
            Map<String, Integer> itemCounts = monthItemCounts.get(month);

            String[] items = visit.getItems().split(",");
            for (String item : items) {
                item = item.trim();
                if (!item.isEmpty()) {
                    itemCounts.put(item, itemCounts.getOrDefault(item, 0) + 1);
                }
            }
        }

        // Flatten into list of rows [month, item, count]
        List<Object[]> result = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, Integer>> monthEntry : monthItemCounts.entrySet()) {
            int month = monthEntry.getKey();
            Map<String, Integer> counts = monthEntry.getValue();
            for (Map.Entry<String, Integer> itemEntry : counts.entrySet()) {
                result.add(new Object[]{month, itemEntry.getKey(), itemEntry.getValue()});
            }
        }

        return result;
    }

    /**
     * Compute total weight of food given out by major
     */
    private List<Object[]> computeTotalWeightByMajor(List<Visit> filteredVisits) {
        Map<String, Double> majorWeightMap = new HashMap<>();
        List<Inventory> allInventory = inventoryRepository.findAll();

        for (Visit visit : filteredVisits) {
            Student student = visit.getStudent();
            if (student == null || visit.getItems() == null || visit.getItems().isEmpty()) continue;

            String major = student.getMajor();
            majorWeightMap.putIfAbsent(major, 0.0);
            String[] itemsArray = visit.getItems().split(",");
            for (String itemName : itemsArray) {
                String trimmedItem = itemName.trim();
                if (!trimmedItem.isEmpty()) {
                    // Find the inventory item by product name
                    String queryItem = trimmedItem;
                    Inventory inventoryItem = allInventory.stream()
                            .filter(inv -> inv.getProductName().equalsIgnoreCase(queryItem))
                            .findFirst()
                            .orElse(null);

                    if (inventoryItem != null) {
                        Double weight = inventoryItem.getNetWeight();
                        if (weight > 0) {
                            majorWeightMap.put(major, majorWeightMap.get(major) + weight);
                        }
                    }
                }
            }
        }

        List<Object[]> result = new ArrayList<>();
        double totalWeight = 0.0;
        for (Map.Entry<String, Double> entry : majorWeightMap.entrySet()) {
            result.add(new Object[]{entry.getKey(), String.format("%.2f", entry.getValue())});
            totalWeight += entry.getValue();
        }
        // Add overall total at the end
        result.add(new Object[]{"TOTAL", String.format("%.2f", totalWeight)});

        return result;
    }

    /**
     * Filter visits by date range
     */
    private List<Visit> filterVisitsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Visit> allVisits = visitRepository.findAll();
        
        return allVisits.stream().filter(visit -> {
            if (visit.getTimestamp() == null) return false;
            LocalDate visitDate = visit.getTimestamp().toLocalDate();
            
            // If both dates provided
            if (startDate != null && endDate != null) {
                return !visitDate.isBefore(startDate) && !visitDate.isAfter(endDate);
            }
            // If only start date provided
            else if (startDate != null) {
                return !visitDate.isBefore(startDate);
            }
            // If only end date provided
            else if (endDate != null) {
                return !visitDate.isAfter(endDate);
            }
            // If neither provided, include all
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * Gather all report data for HTML or PDF
     */
    private Map<String, Object> getReportDataMap(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = new HashMap<>();

        // Filter visits by date range
        List<Visit> filteredVisits = filterVisitsByDateRange(startDate, endDate);

        List<Inventory> inventoryList = inventoryRepository.findAll();
        int totalItems = inventoryList.stream().mapToInt(Inventory::getQuantity).sum();
        long lowStockCount = inventoryList.stream().filter(i -> i.getQuantity() <= 5).count();

        data.put("inventoryList", inventoryList);
        data.put("totalItems", totalItems);
        data.put("lowStockCount", lowStockCount);
        data.put("startDate", startDate);
        data.put("endDate", endDate);

        // Visits summary by major (filtered)
        // Need to compute this from filteredVisits
        Map<String, Long> visitsByMajorMap = filteredVisits.stream()
                .filter(v -> v.getStudent() != null && v.getStudent().getMajor() != null)
                .collect(Collectors.groupingBy(v -> v.getStudent().getMajor(), Collectors.counting()));
        
        List<Object[]> visitsByMajor = visitsByMajorMap.entrySet().stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .collect(Collectors.toList());
        
        data.put("visitsByMajor", visitsByMajor);

        // Popular items by major (filtered)
        data.put("itemsByMajor", computeItemsByMajor(filteredVisits));

        // Items per month (filtered)
        data.put("itemsPerMonth", computeItemsPerMonth(filteredVisits));

        // Total weight by major (filtered)
        data.put("weightByMajor", computeTotalWeightByMajor(filteredVisits));

        return data;
    }

    @GetMapping("/report")
    public String reportPage(@RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             Model model) {
        LocalDate start = null;
        LocalDate end = null;
        
        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDate.parse(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDate.parse(endDate);
        }
        
        model.addAllAttributes(getReportDataMap(start, end));
        return "report";
    }

    @GetMapping("/report/download")
    public ResponseEntity<byte[]> downloadReport(@RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate) {
        LocalDate start = null;
        LocalDate end = null;
        
        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDate.parse(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDate.parse(endDate);
        }
        
        // Use separate PDF template
        byte[] pdfBytes = pdfService.generatePdfFromHtml("report-pdf", getReportDataMap(start, end));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Pantry_Report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
