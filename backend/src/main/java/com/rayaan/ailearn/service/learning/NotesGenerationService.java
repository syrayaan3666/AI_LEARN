package com.rayaan.ailearn.service.learning;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rayaan.ailearn.dto.response.VideoResourceResponse;
import com.rayaan.ailearn.model.Topic;

@Service
public class NotesGenerationService {

    private final NotesAiAgent notesAiAgent;

    public NotesGenerationService(NotesAiAgent notesAiAgent) {
        this.notesAiAgent = notesAiAgent;
    }

    public String generateNotesForTopic(Topic topic) {
        // Delegate to the AI agent for actual notes generation
        return notesAiAgent.generateNotesForTopic(topic);
    }

    public List<VideoResourceResponse> generateVideoLinksForTopic(Topic topic) {
        return notesAiAgent.generateVideoLinksForTopic(topic);
    }

    public String generatePlaceholderNotes(Topic topic) {
        return "Auto-generated starter notes for " + topic.getName() + ". Focus on core concepts, examples, and quiz practice.";
    }

    public List<VideoResourceResponse> generatePlaceholderResources(Topic topic) {
        String base = topic.getName().replace(' ', '+');
        return List.of(
                new VideoResourceResponse("YouTube results for " + topic.getName(), "https://www.youtube.com/results?search_query=" + base),
                new VideoResourceResponse("Google tutorial search for " + topic.getName(), "https://www.google.com/search?q=" + base + "+tutorial")
        );
    }
}
