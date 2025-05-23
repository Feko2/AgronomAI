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

            if (historicalData.isEmpty()) {
                return Map.of("error", "No hay datos suficientes para generar el reporte");
            }

            // Generar reporte Markdown con IA
            String markdownContent = generateMarkdownReport(parcela, historicalData);
            
            // Estructura de respuesta
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("format", "markdown");
            exportData.put("filename", String.format("Reporte_Agronomico_%s_%s.md", 
                parcela.getNombre().replaceAll("\\s+", "_"), 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            exportData.put("content", markdownContent);
            exportData.put("contentType", "text/markdown");
            
            return exportData;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Error al generar reporte: " + e.getMessage());
        }
    }

    private String generateAIInsightsText(Parcela parcela, List<SensorData> data, Map<String, Object> estadisticas) {
        try {
            Map<String, Object> promedios = (Map<String, Object>) estadisticas.get("promedios");
            Map<String, Object> tendencias = (Map<String, Object>) estadisticas.get("tendencias");
            Map<String, Object> rangos = (Map<String, Object>) estadisticas.get("rangos");
            
            String prompt = String.format("""
                Analiza estos datos agronómicos y genera un reporte ejecutivo en español para un manager de agricultura.
                
                **PARCELA:** %s (%s)
                **CULTIVO:** %s
                **ÁREA:** %.2f hectáreas
                **PERÍODO:** Últimos 30 días (%d lecturas)
                
                **PROMEDIOS:**
                • Humedad: %.1f%%
                • pH: %.2f
                • Nitrógeno: %.1f ppm
                • Temperatura: %.1f°C
                
                **RANGOS:**
                • Humedad: %.1f%% - %.1f%%
                • Temperatura: %.1f°C - %.1f°C
                
                Genera un análisis estructurado con:
                
                ## 🎯 RESUMEN EJECUTIVO
                (2-3 líneas sobre el estado general de la parcela)
                
                ## 📊 ANÁLISIS DETALLADO
                
                ### Condiciones del Suelo
                (Evalúa humedad, pH, nitrógeno según estándares para %s)
                
                ### Condiciones Ambientales  
                (Evalúa temperatura y su impacto en el cultivo)
                
                ## ⚠️ ALERTAS Y RIESGOS
                (Identifica problemas críticos que requieren atención inmediata)
                
                ## 🚀 RECOMENDACIONES PRIORITARIAS
                (3-4 acciones específicas y prácticas para mejorar el rendimiento)
                
                ## 📈 PERSPECTIVA A FUTURO
                (Proyecciones y próximos pasos recomendados)
                
                **IMPORTANTE:** Usa lenguaje claro y técnico pero accesible. Incluye emojis para hacer más visual. Sé específico en las recomendaciones.
                """,
                parcela.getNombre(),
                parcela.getParcelaId(),
                parcela.getTipoCultivo(),
                parcela.getAreaHectareas(),
                data.size(),
                (Double) promedios.get("humedad"),
                (Double) promedios.get("ph"),
                (Double) promedios.get("nitrogeno"),
                (Double) promedios.get("temperatura"),
                ((Map<String, Double>) rangos.get("humedad")).get("min"),
                ((Map<String, Double>) rangos.get("humedad")).get("max"),
                ((Map<String, Double>) rangos.get("temperatura")).get("min"),
                ((Map<String, Double>) rangos.get("temperatura")).get("max"),
                parcela.getTipoCultivo()
            );

            // Llamar a la API de OpenAI (si está configurada)
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                return callOpenAIForInsights(prompt, parcela, data, estadisticas);
            } else {
                // Fallback: generar insights básicos sin IA externa
                return generateBasicInsights(parcela, data, estadisticas);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return generateBasicInsights(parcela, data, estadisticas);
        }
    }

    private String callOpenAIForInsights(String prompt, Parcela parcela, List<SensorData> data, Map<String, Object> estadisticas) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "system", "content", "Eres un agrónomo experto que analiza datos de cultivos y genera reportes ejecutivos claros y actionables en español."),
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1500);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);

            String content = ((Map<String, String>) ((List<Map<String, Object>>) response.get("choices")).get(0).get("message")).get("content");
            return content;

        } catch (Exception e) {
            e.printStackTrace();
            // En caso de error con IA, usar análisis básico
            return generateBasicInsights(parcela, data, estadisticas);
        }
    }

    private String generateBasicInsights(Parcela parcela, List<SensorData> data, Map<String, Object> estadisticas) {
        Map<String, Object> promedios = (Map<String, Object>) estadisticas.get("promedios");
        StringBuilder insights = new StringBuilder();
        
        insights.append("## 🎯 RESUMEN EJECUTIVO\n\n");
        insights.append(String.format("La parcela %s presenta condiciones **%s** con %d lecturas analizadas en los últimos 30 días. ", 
            parcela.getNombre(), 
            determineOverallStatus((Double) promedios.get("humedad"), (Double) promedios.get("ph"), (Double) promedios.get("nitrogeno"), parcela),
            data.size()));
        insights.append("El análisis indica un estado general estable que requiere monitoreo continuo.\n\n");
        
        insights.append("## 📊 ANÁLISIS DETALLADO\n\n");
        insights.append("### Condiciones del Suelo\n");
        insights.append(String.format("- **Humedad promedio:** %.1f%% %s\n", 
            (Double) promedios.get("humedad"),
            (Double) promedios.get("humedad") < 50 ? "⚠️ Nivel bajo, considerar aumento de riego" : 
            (Double) promedios.get("humedad") > 80 ? "⚠️ Nivel alto, revisar drenaje" : "✅ Nivel adecuado"));
        insights.append(String.format("- **pH promedio:** %.2f %s\n", 
            (Double) promedios.get("ph"),
            (Double) promedios.get("ph") < 6.0 ? "⚠️ Suelo ácido, considerar encalado" : 
            (Double) promedios.get("ph") > 7.5 ? "⚠️ Suelo alcalino, considerar acidificación" : "✅ Rango óptimo"));
        insights.append(String.format("- **Nitrógeno promedio:** %.1f ppm %s\n", 
            (Double) promedios.get("nitrogeno"),
            (Double) promedios.get("nitrogeno") < 100 ? "⚠️ Nivel bajo, aplicar fertilizante nitrogenado" : "✅ Nivel adecuado"));

        insights.append("\n### Condiciones Ambientales\n");
        insights.append(String.format("- **Temperatura promedio:** %.1f°C %s\n", 
            (Double) promedios.get("temperatura"),
            (Double) promedios.get("temperatura") < 15 ? "❄️ Temperaturas bajas, proteger cultivo" : 
            (Double) promedios.get("temperatura") > 35 ? "🔥 Temperaturas altas, aumentar riego" : "✅ Rango ideal"));

        insights.append("\n\n## ⚠️ ALERTAS Y RIESGOS\n\n");
        if ((Double) promedios.get("humedad") < 40 || (Double) promedios.get("humedad") > 90) {
            insights.append("🚨 **ALERTA CRÍTICA:** Niveles de humedad fuera del rango seguro\n");
        }
        if ((Double) promedios.get("ph") < 5.5 || (Double) promedios.get("ph") > 8.0) {
            insights.append("🚨 **ALERTA CRÍTICA:** pH del suelo en niveles extremos\n");
        }
        if ((Double) promedios.get("temperatura") < 10 || (Double) promedios.get("temperatura") > 40) {
            insights.append("🚨 **ALERTA CRÍTICA:** Temperaturas extremas detectadas\n");
        }
        if (!insights.toString().contains("ALERTA CRÍTICA")) {
            insights.append("✅ No se detectaron alertas críticas en el período analizado\n");
        }

        insights.append("\n## 🚀 RECOMENDACIONES PRIORITARIAS\n\n");
        insights.append("1. **Monitoreo semanal** de todos los parámetros del suelo\n");
        insights.append("2. **Calibración de sensores** para mantener precisión en las mediciones\n");
        insights.append("3. **Análisis de suelo** complementario cada 2 meses\n");
        
        if ((Double) promedios.get("humedad") < 50) {
            insights.append("4. **Aumentar frecuencia de riego** para mejorar niveles de humedad\n");
        } else if ((Double) promedios.get("humedad") > 80) {
            insights.append("4. **Mejorar sistema de drenaje** para reducir exceso de humedad\n");
        } else {
            insights.append("4. **Mantener programa de riego actual** - niveles óptimos\n");
        }

        insights.append("\n## 📈 PERSPECTIVA A FUTURO\n\n");
        insights.append("Basado en los datos actuales, se recomienda:\n\n");
        insights.append("- **Corto plazo (1-2 semanas):** Continuar monitoreo diario y aplicar recomendaciones inmediatas\n");
        insights.append("- **Mediano plazo (1 mes):** Evaluar efectividad de las medidas implementadas\n");
        insights.append("- **Largo plazo (3 meses):** Análisis de tendencias estacionales y ajuste del plan de manejo\n");

        return insights.toString();
    }

    private String getStatusIcon(Double value, Double min, Double max) {
        if (value == null) return "❓ Sin datos";
        if (value < min) return "🔴 Bajo";
        if (value > max) return "🟡 Alto";
        return "🟢 Óptimo";
    }

    private Map<String, Object> calcularEstadisticasCompletas(List<SensorData> data) {
        Map<String, Object> stats = new HashMap<>();
        
        if (data.isEmpty()) {
            return stats;
        }

        // Promedios
        double avgHumedad = data.stream().filter(s -> s.getHumedad() != null).mapToDouble(SensorData::getHumedad).average().orElse(0.0);
        double avgPh = data.stream().filter(s -> s.getPh() != null).mapToDouble(SensorData::getPh).average().orElse(0.0);
        double avgNitrogeno = data.stream().filter(s -> s.getNitrogeno() != null).mapToDouble(SensorData::getNitrogeno).average().orElse(0.0);
        double avgTemperatura = data.stream().filter(s -> s.getTemperatura() != null).mapToDouble(SensorData::getTemperatura).average().orElse(0.0);

        // Rangos
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

        // Tendencias simples (últimos 7 días vs anteriores)
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        List<SensorData> recent = data.stream().filter(s -> s.getFecha().isAfter(cutoff)).collect(Collectors.toList());
        List<SensorData> previous = data.stream().filter(s -> s.getFecha().isBefore(cutoff)).collect(Collectors.toList());
        
        Map<String, String> tendencias = new HashMap<>();
        if (!recent.isEmpty() && !previous.isEmpty()) {
            double recentHumedad = recent.stream().mapToDouble(SensorData::getHumedad).average().orElse(0.0);
            double previousHumedad = previous.stream().mapToDouble(SensorData::getHumedad).average().orElse(0.0);
            tendencias.put("humedad", recentHumedad > previousHumedad ? "Aumentando" : "Disminuyendo");
        }
        stats.put("tendencias", tendencias);

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

    private String generateMarkdownReport(Parcela parcela, List<SensorData> data) {
        // Generar análisis estadístico
        Map<String, Object> estadisticas = calcularEstadisticasCompletas(data);
        
        // Generar insights AI
        String aiInsights = generateAIInsightsText(parcela, data, estadisticas);
        
        StringBuilder markdown = new StringBuilder();
        
        // Header del reporte
        markdown.append("# 🌱 REPORTE AGRONÓMICO\n\n");
        markdown.append("---\n\n");
        
        // Información de la parcela
        markdown.append("## 📋 INFORMACIÓN DE LA PARCELA\n\n");
        markdown.append("| Campo | Valor |\n");
        markdown.append("|-------|-------|\n");
        markdown.append(String.format("| **Nombre** | %s |\n", parcela.getNombre()));
        markdown.append(String.format("| **ID** | %s |\n", parcela.getParcelaId()));
        markdown.append(String.format("| **Cultivo** | %s |\n", parcela.getTipoCultivo()));
        markdown.append(String.format("| **Área** | %.2f hectáreas |\n", parcela.getAreaHectareas()));
        markdown.append(String.format("| **Ubicación** | %s |\n", parcela.getUbicacion()));
        markdown.append(String.format("| **Fecha de Reporte** | %s |\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        markdown.append(String.format("| **Período Analizado** | Últimos 30 días |\n"));
        markdown.append(String.format("| **Total de Lecturas** | %d |\n", data.size()));
        
        markdown.append("\n---\n\n");
        
        // Estadísticas principales
        Map<String, Object> promedios = (Map<String, Object>) estadisticas.get("promedios");
        Map<String, Object> rangos = (Map<String, Object>) estadisticas.get("rangos");
        
        markdown.append("## 📊 ESTADÍSTICAS PRINCIPALES\n\n");
        markdown.append("### Valores Promedio\n\n");
        markdown.append("| Parámetro | Valor | Estado |\n");
        markdown.append("|-----------|-------|--------|\n");
        markdown.append(String.format("| 💧 **Humedad** | %.1f%% | %s |\n", 
            (Double) promedios.get("humedad"),
            getStatusIcon((Double) promedios.get("humedad"), 40.0, 85.0)));
        markdown.append(String.format("| ⚗️ **pH** | %.2f | %s |\n", 
            (Double) promedios.get("ph"),
            getStatusIcon((Double) promedios.get("ph"), 6.0, 7.5)));
        markdown.append(String.format("| 🌿 **Nitrógeno** | %.1f ppm | %s |\n", 
            (Double) promedios.get("nitrogeno"),
            getStatusIcon((Double) promedios.get("nitrogeno"), 100.0, 200.0)));
        markdown.append(String.format("| 🌡️ **Temperatura** | %.1f°C | %s |\n", 
            (Double) promedios.get("temperatura"),
            getStatusIcon((Double) promedios.get("temperatura"), 18.0, 32.0)));
        
        markdown.append("\n### Rangos Observados\n\n");
        Map<String, Double> humedadRange = (Map<String, Double>) rangos.get("humedad");
        Map<String, Double> tempRange = (Map<String, Double>) rangos.get("temperatura");
        
        markdown.append("| Parámetro | Mínimo | Máximo | Variación |\n");
        markdown.append("|-----------|--------|--------|-----------|\n");
        markdown.append(String.format("| 💧 **Humedad** | %.1f%% | %.1f%% | %.1f%% |\n", 
            humedadRange.get("min"), humedadRange.get("max"), humedadRange.get("max") - humedadRange.get("min")));
        markdown.append(String.format("| 🌡️ **Temperatura** | %.1f°C | %.1f°C | %.1f°C |\n", 
            tempRange.get("min"), tempRange.get("max"), tempRange.get("max") - tempRange.get("min")));
        
        markdown.append("\n---\n\n");
        
        // Insights de IA
        markdown.append(aiInsights);
        
        markdown.append("\n\n---\n\n");
        
        // Footer
        markdown.append("## 📞 CONTACTO Y SEGUIMIENTO\n\n");
        markdown.append("Este reporte fue generado automáticamente por **AgroApp** con análisis de inteligencia artificial.\n\n");
        markdown.append("Para consultas técnicas o seguimiento de recomendaciones, contacte al equipo agronómico.\n\n");
        markdown.append("---\n");
        markdown.append("*Reporte generado el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")) + "*");
        
        return markdown.toString();
    }
} 