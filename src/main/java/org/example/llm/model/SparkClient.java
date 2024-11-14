package org.example.llm.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SparkClient extends AbstractModelClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String modelName;
    private final int maxTokens;
    private final double temperature;

    public SparkClient(String apiKey, String apiUrl) {
        super(apiKey, apiUrl);
        this.modelName = "4.0Ultra";
        this.maxTokens = 8192;
        this.temperature = 0.2;
    }

    @Override
    protected Map<String, String> buildHeader() {
        return Map.of(
                "Content-Type", "application/json",
                "Authorization", "Bearer " + apiKey
        );
    }

    @Override
    protected String buildRequestBody(String systemPrompt, String userPrompt) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", this.modelName);
        requestBody.put("messages", new Object[] {
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        });
        requestBody.put("max_tokens", this.maxTokens);
        requestBody.put("temperature", this.temperature);

        return objectMapper.writeValueAsString(requestBody);
    }

    @Override
    public String sendRequest(String systemPrompt, String userPrompt) throws Exception {
        return super.sendRequest(systemPrompt, userPrompt);
    }

    @Override
    public String handleResponse(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}