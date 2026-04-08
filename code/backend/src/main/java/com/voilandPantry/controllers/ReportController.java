package com.voilandPantry.controllers;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.voilandPantry.models.*;
import com.voilandPantry.repositories.*;
import com.voilandPantry.services.ReportPdfService;

@Controller
public class ReportController {

    private final InventoryRepository inventoryRepository;
    private final VisitRepository visitRepository;
    private final ReportPdfService pdfService;

    private final VolunteerRepository volunteerRepository;
    private final VolunteerHoursRepository volunteerHoursRepository;

    public ReportController(InventoryRepository inventoryRepository,
                            VisitRepository visitRepository,
                            ReportPdfService pdfService,
                            VolunteerRepository volunteerRepository,
                            VolunteerHoursRepository volunteerHoursRepository) {

        this.inventoryRepository = inventoryRepository;
        this.visitRepository = visitRepository;
        this.pdfService = pdfService;
        this.volunteerRepository = volunteerRepository;
        this.volunteerHoursRepository = volunteerHoursRepository;
    }

    // =========================
    // EXISTING INVENTORY REPORT
    // =========================

    private List<Visit> filterVisitsByDateRange(LocalDate startDate, LocalDate endDate) {
        return visitRepository.findAll().stream().filter(visit -> {
            if (visit.getTimestamp() == null) return false;

            LocalDate visitDate = visit.getTimestamp().toLocalDate();

            if (startDate != null && endDate != null) {
                return !visitDate.isBefore(startDate) && !visitDate.isAfter(endDate);
            } else if (startDate != null) {
                return !visitDate.isBefore(startDate);
            } else if (endDate != null) {
                return !visitDate.isAfter(endDate);
            }
            return true;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> getReportDataMap(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> data = new HashMap<>();

        List<Visit> filteredVisits = filterVisitsByDateRange(startDate, endDate);
        List<Inventory> inventoryList = inventoryRepository.findAll();

        int totalItems = inventoryList.stream().mapToInt(Inventory::getQuantity).sum();
        long lowStockCount = inventoryList.stream().filter(i -> i.getQuantity() <= 5).count();

        data.put("inventoryList", inventoryList);
        data.put("totalItems", totalItems);
        data.put("lowStockCount", lowStockCount);
        data.put("startDate", startDate);
        data.put("endDate", endDate);

        Map<String, Long> visitsByMajorMap = filteredVisits.stream()
                .filter(v -> v.getStudent() != null && v.getStudent().getMajor() != null)
                .collect(Collectors.groupingBy(v -> v.getStudent().getMajor(), Collectors.counting()));

        List<Object[]> visitsByMajor = visitsByMajorMap.entrySet().stream()
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .toList();

        data.put("visitsByMajor", visitsByMajor);

        return data;
    }

    @GetMapping("/report")
    public String reportPage(@RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             Model model) {

        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        model.addAllAttributes(getReportDataMap(start, end));
        return "report";
    }

    @GetMapping("/report/download")
    public ResponseEntity<byte[]> downloadReport(@RequestParam(required = false) String startDate,
                                                  @RequestParam(required = false) String endDate) {

        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        byte[] pdfBytes = pdfService.generatePdfFromHtml("report-pdf", getReportDataMap(start, end));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Pantry_Report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // =========================
    // VOLUNTEER REPORT (NEW)
    // =========================

    private Map<String, Object> getVolunteerReportData(LocalDate startDate, LocalDate endDate) {

        Map<String, Object> data = new HashMap<>();

        List<Volunteer> volunteers = volunteerRepository.findAll();
        List<VolunteerHours> allHours = volunteerHoursRepository.findAll();

        List<VolunteerHours> filtered = allHours.stream().filter(h -> {
            if (h.getDate() == null) return false;

            LocalDate d = LocalDate.parse(h.getDate());

            if (startDate != null && endDate != null) {
                return !d.isBefore(startDate) && !d.isAfter(endDate);
            } else if (startDate != null) {
                return !d.isBefore(startDate);
            } else if (endDate != null) {
                return !d.isAfter(endDate);
            }
            return true;
        }).toList();

        Map<Long, Double> totals = new HashMap<>();

        for (VolunteerHours h : filtered) {
            Long vid = h.getVolunteer().getId();
            totals.put(vid, totals.getOrDefault(vid, 0.0) + h.getHours());
        }

        data.put("volunteers", volunteers);
        data.put("totals", totals);
        data.put("startDate", startDate);
        data.put("endDate", endDate);

        return data;
    }

    @GetMapping("/admin/volunteer-report")
    public String volunteerReportPage(@RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate,
                                      Model model) {

        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        model.addAllAttributes(getVolunteerReportData(start, end));
        return "volunteer_report";
    }

    @GetMapping("/admin/volunteer-report/download")
    public ResponseEntity<byte[]> downloadVolunteerReport(@RequestParam(required = false) String startDate,
                                                          @RequestParam(required = false) String endDate) {

        LocalDate start = (startDate != null && !startDate.isEmpty()) ? LocalDate.parse(startDate) : null;
        LocalDate end = (endDate != null && !endDate.isEmpty()) ? LocalDate.parse(endDate) : null;

        byte[] pdfBytes = pdfService.generatePdfFromHtml(
                "volunteer_report_pdf",
                getVolunteerReportData(start, end)
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Volunteer_Report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}