package com.project.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class ChatGPTService {

    private static final Logger logger = LoggerFactory.getLogger(ChatGPTService.class);
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL_NAME = "gpt-3.5-turbo";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String openaiApiKey;

    public ChatGPTService(ObjectMapper objectMapper, @Value("${openai.api.key}") String openaiApiKey) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = objectMapper;
        this.openaiApiKey = openaiApiKey;
    }

    public String sendToChatGPT(String prompt) throws IOException {
        if (openaiApiKey == null || openaiApiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("OpenAI API key is not set.");
        }

        logger.info("Using OpenAI API Key: {}", openaiApiKey);

        JsonNode requestBody = objectMapper.createObjectNode()
                .put("model", MODEL_NAME)
                .set("messages", objectMapper.createArrayNode()
                        .add(objectMapper.createObjectNode()
                                .put("role", "user")
                                .put("content", prompt)));

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + openaiApiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 429) {
                    throw new IOException("Rate limit exceeded: " + response.message());
                }
                throw new IOException("Unexpected response code " + response.code() + ": " + response.message());
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Response body is null");
            }

            JsonNode jsonResponse = objectMapper.readTree(responseBody.string());
            JsonNode choices = jsonResponse.path("choices");
            if (choices.isMissingNode() || !choices.isArray() || choices.size() == 0) {
                throw new IOException("Invalid response structure: " + jsonResponse.toString());
            }

            return choices.get(0).path("message").path("content").asText();
        } catch (Exception e) {
            logger.error("Error during OpenAI API request", e);
            throw e;
        }
    }
}
