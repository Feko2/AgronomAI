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
} 