package com.felipe.agroapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.felipe.agroapp.model.Parcela;
import com.felipe.agroapp.model.SensorData;
import com.felipe.agroapp.repository.ParcelaRepository;
import com.felipe.agroapp.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightsService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Autowired
    private ParcelaRepository parcelaRepository;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public InsightsService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<Map<String, String>> generateInsights(Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // Create the system prompt
            String systemPrompt = "Eres un asistente experto en agricultura que analiza datos de sensores de cultivos y proporciona recomendaciones prácticas. Responde en español.";
            
            // Create the user prompt with the sensor data
            String userPrompt = String.format("""
                Analiza estos datos de sensores y proporciona 3 recomendaciones o insights importantes:
                %s
                
                Para cada recomendación, incluye:
                1. Un título breve
                2. Una descripción concisa con acción recomendada
                3. Un tipo (success, warning, o alert)
                4. La parcela relevante
                
                Formatea tu respuesta como un array JSON con esta estructura:
                [
                  {
                    "title": "Título del insight",
                    "description": "Descripción y recomendación",
                    "type": "success|warning|alert",
                    "parcela": "ID-PARCELA"
                  }
                ]
                """, 
                objectMapper.writeValueAsString(request.get("sensorData"))
            );

            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 800);

            // Make the API call
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);

            // Parse the response
            String content = ((Map<String, String>) ((List<Map<String, Object>>) response.get("choices")).get(0).get("message")).get("content");
            
            // Extract JSON array from response
            int startIndex = content.indexOf('[');
            int endIndex = content.lastIndexOf(']') + 1;
            String jsonArray = content.substring(startIndex, endIndex);
            
            // Parse JSON array into List<Map>
            return objectMapper.readValue(jsonArray, List.class);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getGeneralInsights() {
        try {
            // Obtener datos recientes de todas las parcelas
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            List<SensorData> recentData = sensorDataRepository.findRecentData(since);
            List<Parcela> parcelas = parcelaRepository.findAll();

            List<Map<String, Object>> insights = new ArrayList<>();

            // Análisis general por parcela
            Map<String, List<SensorData>> dataByParcela = recentData.stream()
                .collect(Collectors.groupingBy(SensorData::getParcelaId));

            for (Parcela parcela : parcelas) {
                List<SensorData> parcelaData = dataByParcela.getOrDefault(parcela.getParcelaId(), Collections.emptyList());
                
                if (!parcelaData.isEmpty()) {
                    Map<String, Object> parcelaInsight = analyzeParcela(parcela, parcelaData);
                    insights.add(parcelaInsight);
                }
            }

            // Agregar estadísticas generales
            Map<String, Object> generalStats = generateGeneralStats(parcelas, recentData);
            insights.add(0, generalStats);

            return insights;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Map<String, Object> getParcelaInsights(String parcelaId) {
        try {
            Optional<Parcela> parcelaOpt = parcelaRepository.findById(parcelaId);
            if (parcelaOpt.isEmpty()) {
                return Map.of("error", "Parcela no encontrada");
            }

            Parcela parcela = parcelaOpt.get();
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            List<SensorData> recentData = sensorDataRepository.findByParcelaIdAndFechaBetween(
                parcelaId, since, LocalDateTime.now()
            );

            return analyzeParcela(parcela, recentData);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Error al obtener insights de la parcela");
        }
    }

    public Map<String, Object> generateDetailedReport(String parcelaId) {
        try {
            Optional<Parcela> parcelaOpt = parcelaRepository.findById(parcelaId);
            if (parcelaOpt.isEmpty()) {
                return Map.of("error", "Parcela no encontrada");
            }

            Parcela parcela = parcelaOpt.get();
            
            // Datos de los últimos 30 días
            LocalDateTime since = LocalDateTime.now().minusDays(30);
            List<SensorData> historicalData = sensorDataRepository.findByParcelaIdAndFechaBetween(
                parcelaId, since, LocalDateTime.now()
            );

            Map<String, Object> report = new HashMap<>();
            report.put("parcela", parcela);
            report.put("fechaGeneracion", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            report.put("periodoAnalisis", "Últimos 30 días");
            report.put("totalLecturas", historicalData.size());

            // Estadísticas detalladas
            if (!historicalData.isEmpty()) {
                report.put("estadisticas", generateDetailedStats(historicalData, parcela));
                report.put("tendencias", analyzeTrends(historicalData));
                report.put("alertas", generateAlerts(historicalData, parcela));
                report.put("recomendaciones", generateRecommendations(historicalData, parcela));
            }

            return report;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Error al generar reporte detallado");
        }
    }

    public Map<String, Object> exportReport(String parcelaId, String format) {
        try {
            // Obtener datos detallados de la parcela
            Optional<Parcela> parcelaOpt = parcelaRepository.findById(parcelaId);
            if (parcelaOpt.isEmpty()) {
                return Map.of("error", "Parcela no encontrada");
            }

            Parcela parcela = parcelaOpt.get();
            
            // Datos de los últimos 30 días para análisis completo
            LocalDateTime since = LocalDateTime.now().minusDays(30);
            List<SensorData> historicalData = sensorDataRepository.findByParcelaIdAndFechaBetween(
                parcelaId, since, LocalDateTime.now()
            );

            // Crear estructura de datos completa para IA
            Map<String, Object> parcelaData = new HashMap<>();
            parcelaData.put("parcelaId", parcela.getParcelaId());
            parcelaData.put("nombre", parcela.getNombre());
            parcelaData.put("tipoCultivo", parcela.getTipoCultivo());
            parcelaData.put("areaHectareas", parcela.getAreaHectareas());
            parcelaData.put("ubicacion", parcela.getUbicacion());

            // Convertir lecturas a formato simple para IA
            List<Map<String, Object>> lecturas = historicalData.stream()
                .map(sensor -> {
                    Map<String, Object> lectura = new HashMap<>();
                    lectura.put("fecha", sensor.getFecha().toString());
                    lectura.put("humedad", sensor.getHumedad());
                    lectura.put("ph", sensor.getPh());
                    lectura.put("nitrogeno", sensor.getNitrogeno());
                    lectura.put("temperatura", sensor.getTemperatura());
                    lectura.put("luminosidad", sensor.getLuminosidad());
                    lectura.put("estado", sensor.getEstado() != null ? sensor.getEstado() : "NORMAL");
                    return lectura;
                })
                .collect(Collectors.toList());

            // Estructura completa para exportación
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("format", format);
            exportData.put("filename", "reporte_" + parcelaId + "_" + 
                          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")));
            
            // Datos estructurados para IA
            Map<String, Object> data = new HashMap<>();
            data.put("parcela", parcelaData);
            data.put("lecturas", lecturas);
            data.put("totalLecturas", lecturas.size());
            data.put("periodoAnalisis", "Últimos 30 días");
            data.put("fechaGeneracion", LocalDateTime.now().toString());
            
            // Agregar estadísticas básicas
            if (!lecturas.isEmpty()) {
                data.put("estadisticasBasicas", calcularEstadisticasBasicas(historicalData));
            }
            
            exportData.put("data", data);
            
            return exportData;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Error al generar datos para exportación: " + e.getMessage());
        }
    }

    private Map<String, Object> calcularEstadisticasBasicas(List<SensorData> data) {
        Map<String, Object> stats = new HashMap<>();
        
        if (data.isEmpty()) {
            return stats;
        }

        // Promedios
        double avgHumedad = data.stream().filter(s -> s.getHumedad() != null).mapToDouble(SensorData::getHumedad).average().orElse(0.0);
        double avgPh = data.stream().filter(s -> s.getPh() != null).mapToDouble(SensorData::getPh).average().orElse(0.0);
        double avgNitrogeno = data.stream().filter(s -> s.getNitrogeno() != null).mapToDouble(SensorData::getNitrogeno).average().orElse(0.0);
        double avgTemperatura = data.stream().filter(s -> s.getTemperatura() != null).mapToDouble(SensorData::getTemperatura).average().orElse(0.0);

        // Máximos y mínimos
        double maxHumedad = data.stream().filter(s -> s.getHumedad() != null).mapToDouble(SensorData::getHumedad).max().orElse(0.0);
        double minHumedad = data.stream().filter(s -> s.getHumedad() != null).mapToDouble(SensorData::getHumedad).min().orElse(0.0);
        
        double maxTemperatura = data.stream().filter(s -> s.getTemperatura() != null).mapToDouble(SensorData::getTemperatura).max().orElse(0.0);
        double minTemperatura = data.stream().filter(s -> s.getTemperatura() != null).mapToDouble(SensorData::getTemperatura).min().orElse(0.0);

        stats.put("promedios", Map.of(
            "humedad", Math.round(avgHumedad * 100.0) / 100.0,
            "ph", Math.round(avgPh * 100.0) / 100.0,
            "nitrogeno", Math.round(avgNitrogeno * 100.0) / 100.0,
            "temperatura", Math.round(avgTemperatura * 100.0) / 100.0
        ));

        stats.put("rangos", Map.of(
            "humedad", Map.of("min", Math.round(minHumedad * 100.0) / 100.0, "max", Math.round(maxHumedad * 100.0) / 100.0),
            "temperatura", Map.of("min", Math.round(minTemperatura * 100.0) / 100.0, "max", Math.round(maxTemperatura * 100.0) / 100.0)
        ));

        return stats;
    }

    private Map<String, Object> analyzeParcela(Parcela parcela, List<SensorData> data) {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("parcelaId", parcela.getParcelaId());
        analysis.put("nombre", parcela.getNombre());
        analysis.put("cultivo", parcela.getTipoCultivo());

        if (data.isEmpty()) {
            analysis.put("status", "Sin datos recientes");
            return analysis;
        }

        // Calcular promedios
        double avgHumedad = data.stream().mapToDouble(SensorData::getHumedad).average().orElse(0.0);
        double avgPh = data.stream().mapToDouble(SensorData::getPh).average().orElse(0.0);
        double avgNitrogeno = data.stream().mapToDouble(SensorData::getNitrogeno).average().orElse(0.0);
        double avgTemperatura = data.stream().mapToDouble(SensorData::getTemperatura).average().orElse(0.0);

        analysis.put("promedios", Map.of(
            "humedad", Math.round(avgHumedad * 100.0) / 100.0,
            "ph", Math.round(avgPh * 100.0) / 100.0,
            "nitrogeno", Math.round(avgNitrogeno * 100.0) / 100.0,
            "temperatura", Math.round(avgTemperatura * 100.0) / 100.0
        ));

        // Determinar estado general
        String status = determineOverallStatus(avgHumedad, avgPh, avgNitrogeno, parcela);
        analysis.put("status", status);

        // Última lectura
        SensorData ultimaLectura = data.get(0); // asumiendo que están ordenados por fecha desc
        analysis.put("ultimaLectura", Map.of(
            "fecha", ultimaLectura.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            "humedad", ultimaLectura.getHumedad(),
            "ph", ultimaLectura.getPh(),
            "nitrogeno", ultimaLectura.getNitrogeno(),
            "temperatura", ultimaLectura.getTemperatura()
        ));

        return analysis;
    }

    private Map<String, Object> generateGeneralStats(List<Parcela> parcelas, List<SensorData> recentData) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("tipo", "estadisticas_generales");
        stats.put("totalParcelas", parcelas.size());
        stats.put("totalLecturas", recentData.size());

        // Distribución por cultivo - filtrar nulls y vacíos
        Map<String, Long> cultivoCount = parcelas.stream()
            .filter(p -> p.getTipoCultivo() != null && !p.getTipoCultivo().trim().isEmpty())
            .collect(Collectors.groupingBy(Parcela::getTipoCultivo, Collectors.counting()));
        stats.put("distribucionCultivos", cultivoCount);

        // Estados de sensores - filtrar nulls y vacíos, usar estado por defecto si es null
        Map<String, Long> estadoCount = recentData.stream()
            .map(s -> s.getEstado() != null && !s.getEstado().trim().isEmpty() ? s.getEstado() : "NORMAL")
            .collect(Collectors.groupingBy(estado -> estado, Collectors.counting()));
        stats.put("distribucionEstados", estadoCount);

        // Agregar estadísticas adicionales
        if (!recentData.isEmpty()) {
            // Promedios generales
            double avgHumedad = recentData.stream()
                .filter(s -> s.getHumedad() != null)
                .mapToDouble(SensorData::getHumedad)
                .average().orElse(0.0);
            
            double avgPh = recentData.stream()
                .filter(s -> s.getPh() != null)
                .mapToDouble(SensorData::getPh)
                .average().orElse(0.0);
            
            double avgNitrogeno = recentData.stream()
                .filter(s -> s.getNitrogeno() != null)
                .mapToDouble(SensorData::getNitrogeno)
                .average().orElse(0.0);
            
            double avgTemperatura = recentData.stream()
                .filter(s -> s.getTemperatura() != null)
                .mapToDouble(SensorData::getTemperatura)
                .average().orElse(0.0);

            stats.put("promediosGenerales", Map.of(
                "humedad", Math.round(avgHumedad * 100.0) / 100.0,
                "ph", Math.round(avgPh * 100.0) / 100.0,
                "nitrogeno", Math.round(avgNitrogeno * 100.0) / 100.0,
                "temperatura", Math.round(avgTemperatura * 100.0) / 100.0
            ));
        }

        return stats;
    }

    private String determineOverallStatus(double humedad, double ph, double nitrogeno, Parcela parcela) {
        try {
            // Verificar si la parcela tiene rangos óptimos definidos
            if (parcela.getHumedadOptimaMin() == null || parcela.getHumedadOptimaMax() == null ||
                parcela.getPhOptimoMin() == null || parcela.getPhOptimoMax() == null ||
                parcela.getNitrogenoOptimoMin() == null || parcela.getNitrogenoOptimoMax() == null) {
                
                // Usar rangos por defecto basados en el tipo de cultivo
                return determineStatusByDefaults(humedad, ph, nitrogeno, parcela.getTipoCultivo());
            }

            boolean humedadOk = humedad >= parcela.getHumedadOptimaMin() && humedad <= parcela.getHumedadOptimaMax();
            boolean phOk = ph >= parcela.getPhOptimoMin() && ph <= parcela.getPhOptimoMax();
            boolean nitrogenoOk = nitrogeno >= parcela.getNitrogenoOptimoMin() && nitrogeno <= parcela.getNitrogenoOptimoMax();

            int okCount = (humedadOk ? 1 : 0) + (phOk ? 1 : 0) + (nitrogenoOk ? 1 : 0);

            if (okCount == 3) return "EXCELENTE";
            if (okCount >= 2) return "BUENO";
            if (okCount == 1) return "REGULAR";
            return "CRITICO";
            
        } catch (Exception e) {
            // En caso de error, usar evaluación por defecto
            return determineStatusByDefaults(humedad, ph, nitrogeno, parcela.getTipoCultivo());
        }
    }

    private String determineStatusByDefaults(double humedad, double ph, double nitrogeno, String tipoCultivo) {
        // Rangos por defecto según tipo de cultivo
        Map<String, Map<String, double[]>> rangosPorCultivo = Map.of(
            "Maíz", Map.of(
                "humedad", new double[]{60.0, 80.0},
                "ph", new double[]{6.0, 7.0},
                "nitrogeno", new double[]{120.0, 180.0}
            ),
            "Trigo", Map.of(
                "humedad", new double[]{50.0, 70.0},
                "ph", new double[]{6.5, 7.5},
                "nitrogeno", new double[]{100.0, 150.0}
            ),
            "Soja", Map.of(
                "humedad", new double[]{55.0, 75.0},
                "ph", new double[]{6.0, 7.0},
                "nitrogeno", new double[]{80.0, 120.0}
            )
        );

        // Usar rangos por defecto (Maíz si no se encuentra el cultivo)
        Map<String, double[]> rangos = rangosPorCultivo.getOrDefault(
            tipoCultivo != null ? tipoCultivo : "Maíz", 
            rangosPorCultivo.get("Maíz")
        );

        boolean humedadOk = humedad >= rangos.get("humedad")[0] && humedad <= rangos.get("humedad")[1];
        boolean phOk = ph >= rangos.get("ph")[0] && ph <= rangos.get("ph")[1];
        boolean nitrogenoOk = nitrogeno >= rangos.get("nitrogeno")[0] && nitrogeno <= rangos.get("nitrogeno")[1];

        int okCount = (humedadOk ? 1 : 0) + (phOk ? 1 : 0) + (nitrogenoOk ? 1 : 0);

        if (okCount == 3) return "EXCELENTE";
        if (okCount >= 2) return "BUENO";
        if (okCount == 1) return "REGULAR";
        return "CRITICO";
    }

    private Map<String, Object> generateDetailedStats(List<SensorData> data, Parcela parcela) {
        // Implementar estadísticas detalladas
        return Map.of("mensaje", "Estadísticas detalladas en desarrollo");
    }

    private Map<String, Object> analyzeTrends(List<SensorData> data) {
        // Implementar análisis de tendencias
        return Map.of("mensaje", "Análisis de tendencias en desarrollo");
    }

    private List<Map<String, Object>> generateAlerts(List<SensorData> data, Parcela parcela) {
        // Implementar generación de alertas
        return List.of(Map.of("mensaje", "Sistema de alertas en desarrollo"));
    }

    private List<Map<String, Object>> generateRecommendations(List<SensorData> data, Parcela parcela) {
        // Implementar generación de recomendaciones
        return List.of(Map.of("mensaje", "Sistema de recomendaciones en desarrollo"));
    }
} 