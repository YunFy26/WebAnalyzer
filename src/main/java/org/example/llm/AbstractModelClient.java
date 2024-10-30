package org.example.llm;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class AbstractModelClient {

    protected String apiKey;

    protected String apiUrl;

    protected Map<String, String> requestHeader;

    protected String requestBody;

    public AbstractModelClient(String apiKey, String apiUrl){
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }


    protected abstract Map<String, String> buildHeader();

    protected abstract String buildRequestBody(String systemPrompt,String userPrompt) throws Exception;

    protected abstract String handleResponse(String responseBody) throws Exception;

    /**
     *
     * @param systemPrompt
     * @param userPrompt
     * @return
     * @throws Exception
     */
    protected String sendRequest(String systemPrompt, String userPrompt) throws Exception {
        // build request body
        String requestBody = buildRequestBody(systemPrompt, userPrompt);
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8));
        // build request header
        buildHeader().forEach(requestBuilder::header);
        // build whole request to send
        HttpRequest request = requestBuilder.build();
        HttpClient client = HttpClient.newHttpClient();
        // send request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200){
            // handle response
            return handleResponse(response.body());
        }else {
            throw new RuntimeException("Field to request, the status code：" + response.statusCode());
        }

    };

}
