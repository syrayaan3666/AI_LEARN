package com.rayaan.ailearn.service.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rayaan.ailearn.model.Topic;

@Service
public class NotesAiAgent {

    private static final Logger logger = LoggerFactory.getLogger(NotesAiAgent.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${app.llm.api-key-groq:}")
    private String groqApiKey;
    
    @Value("${app.llm.provider:groq}")
    private String llmProvider;

    @Value("${app.llm.groq-model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Value("${app.llm.groq-fallback-models:llama-3.1-8b-instant}")
    private String groqFallbackModels;

    public NotesAiAgent(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String generateNotesForTopic(Topic topic) {
        logger.info("Generating notes for topic: {}", topic.getName());

        // Check if API key is configured
        if (groqApiKey == null || groqApiKey.isBlank()) {
            logger.warn("Groq API key not configured. Returning structured fallback notes.");
            return buildStructuredFallbackJson(topic, "Groq API key is not configured.");
        }

        try {
            String prompt = buildNotesPrompt(topic);
            String notes = callLlmAPI(prompt);
            logger.info("Successfully generated notes for topic: {}", topic.getName());
            return notes;
        } catch (Exception e) {
            logger.error("Failed to generate notes for topic: {}", topic.getName(), e);
            // Always return valid JSON so notes_json stays parseable.
            return buildStructuredFallbackJson(topic, "Generation failed. Try again in a moment.");
        }
    }

    public List<String> generateVideoLinksForTopic(Topic topic) {
        logger.info("Generating video links for topic: {}", topic.getName());

        if (groqApiKey == null || groqApiKey.isBlank()) {
            logger.warn("Groq API key not configured. Returning no video links.");
            return List.of();
        }

        try {
            String prompt = buildVideoLinksPrompt(topic);
            String jsonResponse = callLlmAPI(prompt);
            return extractVideoLinks(jsonResponse);
        } catch (Exception e) {
            logger.warn("Failed to generate video links for topic: {}", topic.getName());
            return List.of();
        }
    }

    private String buildNotesPrompt(Topic topic) {
                List<String> subtopics = extractSubtopics(topic.getDescription());
                String subtopicsBlock = subtopics.isEmpty()
                                ? "- No explicit subtopics provided"
                                : subtopics.stream().map(s -> "- " + s).collect(Collectors.joining("\n"));

        return String.format(
            """
                        You are an expert university professor and curriculum writer.

                        Create complete, exam-ready study notes for the topic below.

                        Topic: %s
                        Difficulty Level: %s
                        Topic Description: %s
                        Subtopics:
                        %s

                        Output MUST be valid JSON only.
                        Do not wrap in markdown.
                        Do not include any text outside JSON.

                        Required JSON schema:
                        {
                            "introduction": "string",
                            "core_concepts": [
                                {
                                    "name": "string",
                                    "explanation": "string",
                                    "key_points": ["string", "string"]
                                }
                            ],
                            "real_world_examples": [
                                {
                                    "title": "string",
                                    "description": "string"
                                }
                            ],
                            "step_by_step": ["string", "string"],
                            "key_terms": [
                                {
                                    "term": "string",
                                    "definition": "string"
                                }
                            ],
                            "common_mistakes": ["string", "string"],
                            "practice_questions": [
                                {
                                    "question": "string",
                                    "answer": "string"
                                }
                            ],
                            "summary": "string"
                        }

                        Quality requirements:
                        - Beginner-friendly but technically accurate.
                        - Cover all listed subtopics thoroughly.
                        - Avoid placeholders and generic filler.
                        - Give concrete, practical explanations.
            """,
            topic.getName(),
                        topic.getDifficulty() != null ? topic.getDifficulty() : "Intermediate",
                        topic.getDescription() != null ? topic.getDescription() : "N/A",
                        subtopicsBlock
        );
    }

        private String buildVideoLinksPrompt(Topic topic) {
                return String.format(
                                """
                                You are a learning assistant.

                                Topic: %s
                                Description: %s

                                Return ONLY valid JSON with this exact schema:
                                {
                                    "video_links": ["https://...", "https://...", "https://..."]
                                }

                                Rules:
                                - Provide 4 to 6 high-quality links relevant to the topic.
                                - Prefer YouTube educational links.
                                - No duplicate links.
                                - No extra keys.
                                - No markdown, no explanation.
                                """,
                                topic.getName(),
                                topic.getDescription() != null ? topic.getDescription() : "N/A"
                );
        }

        private List<String> extractSubtopics(String description) {
                if (description == null || description.isBlank()) {
                        return List.of();
                }
                return List.of(description.split(","))
                                .stream()
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .toList();
        }

    private String callLlmAPI(String prompt) {
        if ("groq".equalsIgnoreCase(llmProvider)) {
            return callGroq(prompt);
        } else {
            logger.warn("Unknown LLM provider: {}. Returning structured fallback.", llmProvider);
            return buildStructuredFallbackJson(null, "Unsupported LLM provider configuration.");
        }
    }

    private String callGroq(String prompt) {
        List<String> candidates = buildGroqModelCandidates();
        RuntimeException lastError = null;

        for (String modelName : candidates) {
            try {
                return callGroqWithModel(prompt, modelName);
            } catch (HttpClientErrorException.BadRequest ex) {
                String body = ex.getResponseBodyAsString();
                if (body != null && body.contains("model_decommissioned")) {
                    logger.warn("Groq model {} is decommissioned, trying next fallback model", modelName);
                    lastError = new RuntimeException("Model decommissioned: " + modelName, ex);
                    continue;
                }
                throw ex;
            } catch (RuntimeException ex) {
                lastError = ex;
            }
        }

        if (lastError != null) {
            throw lastError;
        }
        throw new RuntimeException("No available Groq model succeeded.");
    }

    private String callGroqWithModel(String prompt, String modelName) {
        try {
            String url = "https://api.groq.com/openai/v1/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + groqApiKey);
            
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", modelName);
            payload.put("messages", new Object[]{message});
            payload.put("temperature", 0.3);
            payload.put("max_tokens", 2000);
            payload.put("response_format", Map.of("type", "json_object"));
            
            String requestBody = objectMapper.writeValueAsString(payload);
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            logger.debug("Calling Groq API for notes generation with model {}", modelName);
            String response = restTemplate.postForObject(url, request, String.class);
            
            // Parse response to extract message content
            if (response == null) {
                throw new RuntimeException("Empty response from Groq API");
            }
            
            JsonNode responseNode = objectMapper.readTree(response);
            JsonNode choicesNode = responseNode.path("choices");
            
            if (choicesNode.isMissingNode() || choicesNode.isEmpty()) {
                throw new RuntimeException("No choices in Groq API response");
            }
            
            String content = choicesNode.get(0).path("message").path("content").asText();
            
            if (content == null || content.isBlank()) {
                throw new RuntimeException("Empty content in Groq API response");
            }

            // Validate and normalize to strict JSON string for DB storage.
            JsonNode normalized = objectMapper.readTree(content);
            content = objectMapper.writeValueAsString(normalized);
            
            logger.info("Groq API call successful");
            return content;
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse Groq API response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse Groq API response: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            logger.error("Groq API error: {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<String> buildGroqModelCandidates() {
        List<String> fallback = List.of(groqFallbackModels.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        List<String> candidates = new java.util.ArrayList<>();
        if (groqModel != null && !groqModel.isBlank()) {
            candidates.add(groqModel.trim());
        }
        for (String model : fallback) {
            if (!candidates.contains(model)) {
                candidates.add(model);
            }
        }
        return candidates.isEmpty() ? List.of("llama-3.3-70b-versatile") : candidates;
    }

    private String buildStructuredFallbackJson(Topic topic, String reason) {
        String topicName = topic != null && topic.getName() != null ? topic.getName() : "Current Topic";
        String difficulty = topic != null && topic.getDifficulty() != null ? topic.getDifficulty() : "Intermediate";
        Map<String, Object> json = Map.of(
                "introduction", "Unable to generate full notes right now for " + topicName + ".",
                "core_concepts", List.of(),
                "real_world_examples", List.of(),
                "step_by_step", List.of(),
                "key_terms", List.of(),
                "common_mistakes", List.of(),
                "practice_questions", List.of(),
                "summary", "Reason: " + reason + " Difficulty: " + difficulty + "."
        );
        try {
            return objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException ex) {
            return "{\"introduction\":\"Notes generation unavailable\",\"summary\":\"Please retry shortly.\"}";
        }
    }

    private List<String> extractVideoLinks(String rawJson) {
        try {
            if (rawJson == null || rawJson.isBlank()) {
                return List.of();
            }
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode linksNode = root.path("video_links");
            if (!linksNode.isArray() || linksNode.isEmpty()) {
                return List.of();
            }

            List<String> links = new ArrayList<>();
            for (JsonNode linkNode : linksNode) {
                String link = linkNode.asText("").trim();
                if (link.startsWith("http://") || link.startsWith("https://")) {
                    if (!links.contains(link)) {
                        links.add(link);
                    }
                }
            }
            return links;
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            logger.warn("Could not parse generated video links JSON");
            return List.of();
        }
    }

}

