package com.felipe.agroapp.controller;

import com.felipe.agroapp.service.DataGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/data-generator")
@CrossOrigin(origins = "http://localhost:5173")
public class DataGeneratorController {

    @Autowired
    private DataGeneratorService dataGeneratorService;

    @PostMapping("/generate-sample")
    public ResponseEntity<Map<String, String>> generateSampleData() {
        try {
            dataGeneratorService.generateSampleData();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Datos de muestra generados exitosamente",
                "details", "Se crearon 5 parcelas con ~100 lecturas de sensores cada una"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error al generar datos de muestra: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/add-recent")
    public ResponseEntity<Map<String, String>> addRecentReadings() {
        try {
            dataGeneratorService.addRecentReadings();
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Lecturas recientes añadidas exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Error al añadir lecturas recientes: " + e.getMessage()
            ));
        }
    }
} 