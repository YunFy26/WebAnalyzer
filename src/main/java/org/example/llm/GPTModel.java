package org.example.llm;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPTModel extends BaseModel {

    private final String modelName;

    private final int maxTokens;

    private final double temperature;

    private final String systemPrompt;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public GPTModel(String apiKey, String apiUrl) {
        super(apiKey, apiUrl);
        this.modelName = "gpt-3.5-turbo";
        this.maxTokens = 8192;
        this.temperature = 0.8;
        this.systemPrompt = Prompt.SYSTEM_PROMPT.getContent();
    }


    @Override
    protected String buildRequestBody(String userPrompt) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", this.modelName);
        requestBody.put("messages", new Object[] {
                Map.of("role", "system", "content", this.systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        });
        requestBody.put("max_tokens", this.maxTokens);
        requestBody.put("temperature", this.temperature);

        return objectMapper.writeValueAsString(requestBody);
    }


    @Override
    protected String handleResponse(String responseBody) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content"); // 提取模型生成的消息内容
    }
}