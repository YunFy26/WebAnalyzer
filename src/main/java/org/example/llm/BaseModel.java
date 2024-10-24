package org.example.llm;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class BaseModel {

    protected String apiKey;

    protected String apiUrl;

    public BaseModel(String apiKey, String apiUrl){
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    /**
     * Default gpt api
     * @return
     */
    protected Map<String, String> buildHeader(){
        return Map.of(
                "Content-Type", "application/json",
                "Authorization", "Bearer " + apiKey
        );
    }

    protected abstract String buildRequestBody(String prompt) throws Exception;

    protected abstract String handleResponse(String responseBody) throws Exception;

    protected String sendRequest(String userPrompt) throws Exception {
        String requestBody = buildRequestBody(userPrompt);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));

        buildHeader().forEach(requestBuilder::header);

        HttpRequest request = requestBuilder.build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200){
            return handleResponse(response.body());
        }else {
            throw new RuntimeException("Field to request, the status code：" + response.statusCode());
        }

    };

}
