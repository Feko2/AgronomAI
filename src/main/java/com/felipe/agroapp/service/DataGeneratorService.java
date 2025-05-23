package com.felipe.agroapp.service;

import com.felipe.agroapp.model.Parcela;
import com.felipe.agroapp.model.SensorData;
import com.felipe.agroapp.repository.ParcelaRepository;
import com.felipe.agroapp.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class DataGeneratorService {

    @Autowired
    private ParcelaRepository parcelaRepository;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    private final Random random = new Random();

    @Transactional
    public void generateSampleData() {
        // Limpiar datos existentes
        sensorDataRepository.deleteAll();
        parcelaRepository.deleteAll();

        // Crear parcelas de muestra
        List<Parcela> parcelas = createSampleParcelas();
        parcelaRepository.saveAll(parcelas);

        // Generar datos de sensores para cada parcela
        for (Parcela parcela : parcelas) {
            generateSensorDataForParcela(parcela, 100); // 100 lecturas por parcela
        }
    }

    private List<Parcela> createSampleParcelas() {
        return List.of(
            createParcela("PAR-001", "Norte-Maíz", "Maíz", 5.5, "Sector Norte", 
                         45.0, 75.0, 6.0, 7.0, 120.0, 180.0),
            createParcela("PAR-002", "Sur-Trigo", "Trigo", 8.2, "Sector Sur", 
                         30.0, 60.0, 6.2, 7.5, 80.0, 140.0),
            createParcela("PAR-003", "Este-Soja", "Soja", 6.8, "Sector Este", 
                         50.0, 80.0, 6.0, 7.2, 100.0, 160.0),
            createParcela("PAR-004", "Oeste-Girasol", "Girasol", 4.3, "Sector Oeste", 
                         40.0, 70.0, 6.5, 7.8, 90.0, 150.0),
            createParcela("PAR-005", "Centro-Tomate", "Tomate", 2.1, "Sector Centro", 
                         60.0, 85.0, 6.0, 6.8, 150.0, 220.0)
        );
    }

    private Parcela createParcela(String id, String nombre, String cultivo, Double area, 
                                 String ubicacion, Double humedadMin, Double humedadMax,
                                 Double phMin, Double phMax, Double nitMin, Double nitMax) {
        Parcela parcela = new Parcela(id, nombre, cultivo, area, ubicacion);
        parcela.setFechaSiembra(LocalDateTime.now().minusDays(random.nextInt(90) + 30));
        parcela.setHumedadOptimaMin(humedadMin);
        parcela.setHumedadOptimaMax(humedadMax);
        parcela.setPhOptimoMin(phMin);
        parcela.setPhOptimoMax(phMax);
        parcela.setNitrogenoOptimoMin(nitMin);
        parcela.setNitrogenoOptimoMax(nitMax);
        return parcela;
    }

    private void generateSensorDataForParcela(Parcela parcela, int numReadings) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        
        for (int i = 0; i < numReadings; i++) {
            LocalDateTime fecha = startDate.plusHours(i * 7 + random.nextInt(6)); // cada ~7 horas con variación
            
            // Generar valores con variación realista basada en los óptimos de la parcela
            Double humedad = generateRealisticValue(
                parcela.getHumedadOptimaMin(), 
                parcela.getHumedadOptimaMax(), 
                15.0 // variación del 15%
            );
            
            Double ph = generateRealisticValue(
                parcela.getPhOptimoMin(), 
                parcela.getPhOptimoMax(), 
                10.0 // variación del 10%
            );
            
            Double nitrogeno = generateRealisticValue(
                parcela.getNitrogenoOptimoMin(), 
                parcela.getNitrogenoOptimoMax(), 
                20.0 // variación del 20%
            );
            
            // Temperatura típica entre 18-32°C con variación diurna/nocturna
            Double temperatura = 25.0 + (random.nextGaussian() * 4.0);
            temperatura = Math.max(15.0, Math.min(35.0, temperatura));
            
            // Luminosidad típica entre 200-1200 lux (varía mucho según hora del día)
            Double luminosidad = 700.0 + (random.nextGaussian() * 300.0);
            luminosidad = Math.max(100.0, Math.min(1500.0, luminosidad));
            
            // Determinar estado basado en si los valores están en rango óptimo
            String estado = determineStatus(parcela, humedad, ph, nitrogeno);
            
            SensorData sensorData = new SensorData(
                parcela.getParcelaId(), humedad, nitrogeno, ph, temperatura, luminosidad, fecha
            );
            sensorData.setEstado(estado);
            
            sensorDataRepository.save(sensorData);
        }
    }

    private Double generateRealisticValue(Double min, Double max, Double variationPercent) {
        Double optimal = (min + max) / 2.0;
        Double range = max - min;
        Double variation = range * (variationPercent / 100.0);
        
        // Usar distribución normal centrada en el valor óptimo
        Double value = optimal + (random.nextGaussian() * variation);
        
        // Asegurar que esté dentro de límites seguros según el tipo de parámetro
        // Estos límites son más estrictos para evitar constraints de DB
        Double lowerBound, upperBound;
        
        // Detectar el tipo de parámetro basado en los rangos
        if (max <= 14.0) { // Probablemente pH
            lowerBound = Math.max(0.0, min - 2.0);
            upperBound = Math.min(14.0, max + 2.0);
        } else if (max <= 100.0) { // Probablemente humedad
            lowerBound = Math.max(0.0, min - 20.0);
            upperBound = Math.min(100.0, max + 20.0);
        } else { // Probablemente nitrógeno
            lowerBound = Math.max(0.0, min - 30.0);
            upperBound = Math.min(300.0, max + 50.0); // Límite más conservador
        }
        
        return Math.max(lowerBound, Math.min(upperBound, value));
    }

    private String determineStatus(Parcela parcela, Double humedad, Double ph, Double nitrogeno) {
        boolean humedadOk = humedad >= parcela.getHumedadOptimaMin() && humedad <= parcela.getHumedadOptimaMax();
        boolean phOk = ph >= parcela.getPhOptimoMin() && ph <= parcela.getPhOptimoMax();
        boolean nitrogenoOk = nitrogeno >= parcela.getNitrogenoOptimoMin() && nitrogeno <= parcela.getNitrogenoOptimoMax();
        
        int okCount = (humedadOk ? 1 : 0) + (phOk ? 1 : 0) + (nitrogenoOk ? 1 : 0);
        
        if (okCount == 3) return "NORMAL";
        if (okCount >= 2) return "ALERTA";
        return "CRITICO";
    }

    public void addRecentReadings() {
        // Añadir algunas lecturas recientes para simular datos en tiempo real
        List<Parcela> parcelas = parcelaRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        for (Parcela parcela : parcelas) {
            // Solo añadir 1-3 lecturas recientes
            int numReadings = random.nextInt(3) + 1;
            for (int i = 0; i < numReadings; i++) {
                LocalDateTime fecha = now.minusMinutes(random.nextInt(180)); // últimas 3 horas
                
                Double humedad = generateRealisticValue(
                    parcela.getHumedadOptimaMin(), 
                    parcela.getHumedadOptimaMax(), 
                    15.0
                );
                
                Double ph = generateRealisticValue(
                    parcela.getPhOptimoMin(), 
                    parcela.getPhOptimoMax(), 
                    10.0
                );
                
                Double nitrogeno = generateRealisticValue(
                    parcela.getNitrogenoOptimoMin(), 
                    parcela.getNitrogenoOptimoMax(), 
                    20.0
                );
                
                Double temperatura = 25.0 + (random.nextGaussian() * 4.0);
                temperatura = Math.max(15.0, Math.min(35.0, temperatura));
                
                Double luminosidad = 700.0 + (random.nextGaussian() * 300.0);
                luminosidad = Math.max(100.0, Math.min(1500.0, luminosidad));
                
                String estado = determineStatus(parcela, humedad, ph, nitrogeno);
                
                SensorData sensorData = new SensorData(
                    parcela.getParcelaId(), humedad, nitrogeno, ph, temperatura, luminosidad, fecha
                );
                sensorData.setEstado(estado);
                
                sensorDataRepository.save(sensorData);
            }
        }
    }

    private SensorData generateSensorData(Parcela parcela, LocalDateTime baseTime, int dayOffset, int readingIndex) {
        SensorData sensorData = new SensorData();
        
        // Fecha con variación
        LocalDateTime readingTime = baseTime.minusDays(dayOffset)
            .withHour(6 + (readingIndex * 4) % 18) // Entre 6:00 y 24:00
            .withMinute(new Random().nextInt(60));
        
        sensorData.setFecha(readingTime);
        sensorData.setParcelaId(parcela.getParcelaId());
        
        // Generar valores con variación natural pero dentro de rangos seguros
        Random random = new Random();
        
        // Humedad: rango más amplio y seguro (0-100%)
        double humedadBase = (parcela.getHumedadOptimaMin() + parcela.getHumedadOptimaMax()) / 2;
        double humedadVariacion = 5 + random.nextGaussian() * 10; // Variación normal
        double humedad = Math.max(0, Math.min(100, humedadBase + humedadVariacion));
        sensorData.setHumedad(Math.round(humedad * 10.0) / 10.0);
        
        // Nitrógeno: rango seguro (0-200)
        double nitrogenoBase = (parcela.getNitrogenoOptimoMin() + parcela.getNitrogenoOptimoMax()) / 2;
        double nitrogenoVariacion = 3 + random.nextGaussian() * 8; // Variación más controlada
        double nitrogeno = Math.max(0, Math.min(200, nitrogenoBase + nitrogenoVariacion));
        sensorData.setNitrogeno(Math.round(nitrogeno * 10.0) / 10.0);
        
        // pH: rango seguro (0-14)
        double phBase = (parcela.getPhOptimoMin() + parcela.getPhOptimoMax()) / 2;
        double phVariacion = 0.1 + random.nextGaussian() * 0.3; // Variación pequeña
        double ph = Math.max(0, Math.min(14, phBase + phVariacion));
        sensorData.setPh(Math.round(ph * 100.0) / 100.0);
        
        // Temperatura: rango realista (0-50°C)
        double temperaturaBase = 15 + dayOffset * 0.5; // Variación por día
        double temperaturaVariacion = random.nextGaussian() * 5;
        double temperatura = Math.max(0, Math.min(50, temperaturaBase + temperaturaVariacion));
        sensorData.setTemperatura(Math.round(temperatura * 10.0) / 10.0);
        
        // Luminosidad: rango realista (0-100000 lux)
        double luminosidadBase = 30000 + random.nextInt(20000); // Base variable
        double luminosidadVariacion = random.nextGaussian() * 5000;
        double luminosidad = Math.max(0, Math.min(100000, luminosidadBase + luminosidadVariacion));
        sensorData.setLuminosidad((double) Math.round(luminosidad));
        
        // Estado basado en condiciones
        String estado = determineEstado(sensorData, parcela);
        sensorData.setEstado(estado);
        
        return sensorData;
    }

    private String determineEstado(SensorData sensorData, Parcela parcela) {
        boolean humedadOk = sensorData.getHumedad() >= parcela.getHumedadOptimaMin() && 
                           sensorData.getHumedad() <= parcela.getHumedadOptimaMax();
        boolean phOk = sensorData.getPh() >= parcela.getPhOptimoMin() && 
                      sensorData.getPh() <= parcela.getPhOptimoMax();
        boolean nitrogenoOk = sensorData.getNitrogeno() >= parcela.getNitrogenoOptimoMin() && 
                             sensorData.getNitrogeno() <= parcela.getNitrogenoOptimoMax();
        
        int okCount = (humedadOk ? 1 : 0) + (phOk ? 1 : 0) + (nitrogenoOk ? 1 : 0);
        
        if (okCount == 3) return "NORMAL";
        if (okCount >= 2) return "ALERTA";
        return "CRITICO";
    }
} 