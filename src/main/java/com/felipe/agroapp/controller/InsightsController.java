package com.felipe.agroapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.felipe.agroapp.service.InsightsService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/insights")
@CrossOrigin(origins = "http://localhost:5173")
public class InsightsController {

    @Autowired
    private InsightsService insightsService;

    @PostMapping("/analyze")
    public ResponseEntity<List<Map<String, String>>> analyzeData(@RequestBody Map<String, Object> request) {
        try {
            List<Map<String, String>> insights = insightsService.generateInsights(request);
            return ResponseEntity.ok(insights);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getGeneralInsights() {
        try {
            List<Map<String, Object>> insights = insightsService.getGeneralInsights();
            return ResponseEntity.ok(insights);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/parcela/{parcelaId}")
    public ResponseEntity<Map<String, Object>> getParcelaInsights(@PathVariable String parcelaId) {
        try {
            Map<String, Object> insights = insightsService.getParcelaInsights(parcelaId);
            return ResponseEntity.ok(insights);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/report/{parcelaId}")
    public ResponseEntity<Map<String, Object>> getDetailedReport(@PathVariable String parcelaId) {
        try {
            Map<String, Object> report = insightsService.generateDetailedReport(parcelaId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/{parcelaId}")
    public ResponseEntity<Map<String, Object>> exportReport(@PathVariable String parcelaId, 
                                                           @RequestParam(defaultValue = "json") String format) {
        try {
            Map<String, Object> exportData = insightsService.exportReport(parcelaId, format);
            return ResponseEntity.ok(exportData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
} 