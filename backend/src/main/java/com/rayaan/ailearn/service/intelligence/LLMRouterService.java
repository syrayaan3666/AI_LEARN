package com.rayaan.ailearn.service.intelligence;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class LLMRouterService {

    private final RestTemplate restTemplate;
    private final String askUrl;

    public LLMRouterService(RestTemplate restTemplate, @Value("${app.llmrouter.ask-url}") String askUrl) {
        this.restTemplate = restTemplate;
        this.askUrl = askUrl;
    }

    public String ask(String prompt, String context) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "prompt", prompt,
                    "context", context
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    askUrl,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            if (response == null) {
                return "AI service returned no response.";
            }

            Object answer = response.getOrDefault("answer", response.get("response"));
            return answer == null ? "AI service returned an empty answer." : answer.toString();
        } catch (RestClientException ex) {
            return "Fallback AI response: " + ex.getMessage();
        }
    }
}
