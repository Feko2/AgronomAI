package com.felipe.agroapp.controller;

import com.felipe.agroapp.model.SensorData;
import com.felipe.agroapp.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/sensors")
@CrossOrigin(origins = "http://localhost:5173")
public class SensorDataController {
    private static final Logger logger = Logger.getLogger(SensorDataController.class.getName());

    @Autowired
    private SensorDataRepository sensorDataRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<List<SensorData>> getAllSensors() {
        List<SensorData> sensors = sensorDataRepository.findAll();
        logger.info("Retrieved " + sensors.size() + " sensors from database");
        for (SensorData sensor : sensors) {
            logger.info("Sensor: id=" + sensor.getId() + ", parcelaId=" + sensor.getParcelaId() + 
                      ", humedad=" + sensor.getHumedad() + ", nitrogeno=" + sensor.getNitrogeno() + 
                      ", ph=" + sensor.getPh() + ", fecha=" + sensor.getFecha());
        }
        return ResponseEntity.ok(sensors);
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Basic connection test
            long count = sensorDataRepository.count();
            response.put("status", "success");
            response.put("message", "Database connection successful!");
            response.put("recordCount", count);
            
            // Get database metadata
            try {
                String dbType = jdbcTemplate.queryForObject("SELECT 'Oracle ' || REGEXP_SUBSTR(BANNER, '[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+') FROM V$VERSION WHERE ROWNUM = 1", String.class);
                response.put("databaseType", dbType);
            } catch (Exception e) {
                try {
                    // Fallback for H2
                    String dbType = jdbcTemplate.queryForObject("SELECT H2VERSION() FROM DUAL", String.class);
                    response.put("databaseType", "H2 " + dbType);
                } catch (Exception ex) {
                    response.put("databaseType", "Unknown");
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Database connection error: " + e.getMessage());
            response.put("status", "error");
            response.put("message", "Database connection failed: " + e.getMessage());
            response.put("errorType", e.getClass().getName());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 