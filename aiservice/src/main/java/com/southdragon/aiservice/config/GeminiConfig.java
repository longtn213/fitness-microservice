package com.southdragon.aiservice.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Bean
    public Client geminiClient() {
        // Client sẽ tự dùng GEMINI_API_KEY từ env, nhưng bạn có thể set nếu cần
        System.setProperty("GEMINI_API_KEY", geminiApiKey);
        return new Client();
    }
}
