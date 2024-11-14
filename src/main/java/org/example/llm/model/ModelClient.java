package org.example.llm.model;

import java.util.Map;

public interface ModelClient {

    /**
     * Builds request headers.
     */
    Map<String, String> buildHeader();

    /**
     * Builds the request body with system and user prompts.
     */
    String buildRequestBody(String systemPrompt, String userPrompt) throws Exception;

    /**
     * Handles the response returned by the model API.
     */
    String handleResponse(String responseBody) throws Exception;

    /**
     * Sends the request and returns the processed response.
     */
    String sendRequest(String systemPrompt, String userPrompt) throws Exception;
}