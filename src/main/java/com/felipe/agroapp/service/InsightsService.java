package com.felipe.agroapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

@Service
public class InsightsService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

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
} 