package com.southdragon.aiservice.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

//    private final WebClient webClient;
//    @Value("${gemini.api.url}")
//    private String geminiApiUrl;
//
//    public GeminiService(WebClient.Builder webClientBuilder) {
//        this.webClient = webClientBuilder.build();
//    }
//    public String getAnswer(String question) {
//        Map<String, Object> requestBody = Map.of(
//                "contents", List.of(
//                        Map.of("parts", List.of(
//                                Map.of("text", question)
//                        ))
//                )
//        );
//
//        return webClient.post()
//                .uri(geminiApiUrl)
//                .header("x-goog-api-key", geminiApiKey)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//    }
    private final Client client;
    private final String geminiModel;


    public GeminiService(Client client,@Value("${gemini.model.name}") String geminiModel) {
        this.client = client;
        this.geminiModel = geminiModel;
    }

    public String getAnswer(String question) {
        try{
            GenerateContentResponse response = client.models.generateContent(geminiModel, question, null);
            return response.text();
        }catch(Exception ex){
            ex.printStackTrace();
            return "Error: " + ex.getMessage();
        }
    }
}
