package com.rayaan.ailearn.service.learning;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rayaan.ailearn.dto.response.LearningContentResponse;
import com.rayaan.ailearn.dto.response.TopicResponse;
import com.rayaan.ailearn.dto.response.VideoResourceResponse;
import com.rayaan.ailearn.exception.ResourceNotFoundException;
import com.rayaan.ailearn.model.Topic;
import com.rayaan.ailearn.model.TopicContent;
import com.rayaan.ailearn.repository.TopicContentRepository;
import com.rayaan.ailearn.repository.TopicRepository;

@Service
public class TopicContentService {

    private static final Logger logger = LoggerFactory.getLogger(TopicContentService.class);
    private final TopicRepository topicRepository;
    private final TopicContentRepository topicContentRepository;
    private final NotesGenerationService notesGenerationService;
    private final ObjectMapper objectMapper;

    public TopicContentService(
            TopicRepository topicRepository,
            TopicContentRepository topicContentRepository,
            NotesGenerationService notesGenerationService,
            ObjectMapper objectMapper
    ) {
        this.topicRepository = topicRepository;
        this.topicContentRepository = topicContentRepository;
        this.notesGenerationService = notesGenerationService;
        this.objectMapper = objectMapper;
    }

    public List<TopicResponse> getTopics() {
        List<Topic> topics = topicRepository.findByActiveTrue();
        if (topics.isEmpty()) {
            topics = topicRepository.findAll();
        }

        return topics.stream()
                .map(topic -> new TopicResponse(
                        topic.getId(),
                topic.getCurriculumName(),
                        topic.getName(),
                        topic.getDescription(),
                        topic.getDifficulty(),
                        0.0,
                        "IN_PROGRESS"
                ))
                .toList();
    }

    public LearningContentResponse getLearningContent(Long topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));

        TopicContent content = topicContentRepository.findByTopicId(topicId)
                .orElseGet(() -> createPlaceholderContent(topic));

        List<VideoResourceResponse> links = parseLinks(content.getResourcesJson());
        Map<String, Object> notesJson = parseNotesJson(content.getNotesJson());
        return new LearningContentResponse(topic.getId(), topic.getName(), content.getNotes(), links, notesJson);
    }

    public LearningContentResponse generateNotesForTopic(Long topicId) {
        logger.info("Generating notes for topic ID: {}", topicId);
        
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));

        // Generate notes using AI agent
        String generatedNotesJson = notesGenerationService.generateNotesForTopic(topic);
        logger.info("Generated notes JSON for topic: {} (length: {})", topic.getName(), generatedNotesJson.length());

        // Get or create topic content
        TopicContent content = topicContentRepository.findByTopicId(topicId)
                .orElseGet(() -> {
                    TopicContent newContent = new TopicContent();
                    newContent.setTopic(topic);
                    return newContent;
                });

        // Update notes and timestamp
        content.setNotesJson(generatedNotesJson);
        content.setNotes(extractReadableSummary(generatedNotesJson));
        content.setUpdatedAt(LocalDateTime.now());

        // Generate video links from LLM and overwrite any previous links.
        List<VideoResourceResponse> generatedVideoLinks = notesGenerationService.generateVideoLinksForTopic(topic);
        if (!generatedVideoLinks.isEmpty()) {
            try {
                content.setResourcesJson(objectMapper.writeValueAsString(generatedVideoLinks));
            } catch (JsonProcessingException ex) {
                logger.warn("Could not serialize generated video links", ex);
                if (content.getResourcesJson() == null || content.getResourcesJson().isBlank()) {
                    content.setResourcesJson("[]");
                }
            }
        } else if (content.getResourcesJson() == null || content.getResourcesJson().isBlank()) {
            content.setResourcesJson("[]");
        }

        // Save updated content
        content = topicContentRepository.save(content);
        logger.info("Saved generated notes for topic: {}", topic.getName());

        List<VideoResourceResponse> links = parseLinks(content.getResourcesJson());
        Map<String, Object> notesJson = parseNotesJson(content.getNotesJson());
        return new LearningContentResponse(topic.getId(), topic.getName(), content.getNotes(), links, notesJson);
    }

    private TopicContent createPlaceholderContent(Topic topic) {
        TopicContent content = new TopicContent();
        content.setTopic(topic);
        content.setNotes(notesGenerationService.generatePlaceholderNotes(topic));
        content.setNotesJson(placeholderNotesJson(topic));
        content.setResourcesJson("[]");
        content.setUpdatedAt(LocalDateTime.now());
        return topicContentRepository.save(content);
    }

    private Map<String, Object> parseNotesJson(String rawJson) {
        try {
            if (rawJson == null || rawJson.isBlank()) {
                return Map.of();
            }
            return objectMapper.readValue(rawJson, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            logger.warn("Could not parse notes_json, falling back to plain notes");
            return Map.of();
        }
    }

    private String extractReadableSummary(String rawJson) {
        try {
            Map<String, Object> notes = objectMapper.readValue(rawJson, new TypeReference<>() {});
            Object intro = notes.get("introduction");
            Object summary = notes.get("summary");

            String introText = intro == null ? "" : String.valueOf(intro).trim();
            String summaryText = summary == null ? "" : String.valueOf(summary).trim();

            if (!introText.isBlank() && !summaryText.isBlank()) {
                return introText + "\n\n" + summaryText;
            }
            if (!introText.isBlank()) {
                return introText;
            }
            if (!summaryText.isBlank()) {
                return summaryText;
            }
            return "Structured notes generated successfully.";
        } catch (JsonProcessingException ex) {
            logger.warn("Failed to extract summary from notes_json");
            return "Structured notes generated successfully.";
        }
    }

    private String placeholderNotesJson(Topic topic) {
        Map<String, Object> json = Map.of(
                "introduction", "Starter notes for " + topic.getName(),
                "core_concepts", List.of(),
                "real_world_examples", List.of(),
                "step_by_step", List.of(),
                "key_terms", List.of(),
                "common_mistakes", List.of(),
                "practice_questions", List.of(),
                "summary", "Use Generate / Refresh Notes to create full detailed notes."
        );
        try {
            return objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private List<VideoResourceResponse> parseLinks(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(json);
            if (!root.isArray() || root.isEmpty()) {
                return List.of();
            }

            // Legacy format support: ["https://..."]
            if (root.get(0).isTextual()) {
                List<String> rawLinks = objectMapper.readValue(json, new TypeReference<>() {});
                return rawLinks.stream()
                        .filter(link -> link != null && !link.isBlank())
                        .map(link -> new VideoResourceResponse(link, link))
                        .toList();
            }

            // New format support: [{"title":"...","url":"..."}]
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }
}
