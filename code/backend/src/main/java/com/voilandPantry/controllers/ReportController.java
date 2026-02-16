package com.voilandPantry.controllers;

import com.voilandPantry.models.Inventory;
import com.voilandPantry.models.Student;
import com.voilandPantry.models.Visit;
import com.voilandPantry.repositories.InventoryRepository;
import com.voilandPantry.repositories.VisitRepository;
import com.voilandPantry.services.ReportPdfService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

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
     * Compute most popular item per major.
     */
    private List<Object[]> computeItemsByMajor() {
        Map<String, Map<String, Integer>> majorItemCounts = new HashMap<>();

        List<Visit> allVisits = visitRepository.findAll();

        for (Visit visit : allVisits) {
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
     * Compute item counts per month.
     */
    private List<Object[]> computeItemsPerMonth() {
        Map<Integer, Map<String, Integer>> monthItemCounts = new HashMap<>();

        List<Visit> allVisits = visitRepository.findAll();

        for (Visit visit : allVisits) {
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
     * Gather all report data for HTML or PDF
     */
    private Map<String, Object> getReportDataMap() {
        Map<String, Object> data = new HashMap<>();

        List<Inventory> inventoryList = inventoryRepository.findAll();
        int totalItems = inventoryList.stream().mapToInt(Inventory::getQuantity).sum();
        long lowStockCount = inventoryList.stream().filter(i -> i.getQuantity() <= 5).count();

        data.put("inventoryList", inventoryList);
        data.put("totalItems", totalItems);
        data.put("lowStockCount", lowStockCount);

        // Visits summary by major
        data.put("visitsByMajor", visitRepository.visitsByMajor());

        // Popular items by major
        data.put("itemsByMajor", computeItemsByMajor());

        // Items per month
        data.put("itemsPerMonth", computeItemsPerMonth());

        return data;
    }

    @GetMapping("/report")
    public String reportPage(Model model) {
        model.addAllAttributes(getReportDataMap());
        return "report";
    }

    @GetMapping("/report/download")
    public ResponseEntity<byte[]> downloadReport() {
        // Use separate PDF template
        byte[] pdfBytes = pdfService.generatePdfFromHtml("report-pdf", getReportDataMap());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Pantry_Report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
