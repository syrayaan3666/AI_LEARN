package com.rayaan.ailearn.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rayaan.ailearn.dto.response.LearningContentResponse;
import com.rayaan.ailearn.dto.response.TopicResponse;
import com.rayaan.ailearn.service.learning.TopicContentService;

@RestController
@RequestMapping
public class TopicController {

    private final TopicContentService topicContentService;

    public TopicController(TopicContentService topicContentService) {
        this.topicContentService = topicContentService;
    }

    @GetMapping("/topics")
    public ResponseEntity<List<TopicResponse>> getTopics() {
        return ResponseEntity.ok(topicContentService.getTopics());
    }

    @GetMapping("/learning/{topicId}")
    public ResponseEntity<LearningContentResponse> getLearning(@PathVariable Long topicId) {
        return ResponseEntity.ok(topicContentService.getLearningContent(topicId));
    }

    @PostMapping("/learning/{topicId}/generate-notes")
    public ResponseEntity<LearningContentResponse> generateNotes(@PathVariable Long topicId) {
        return ResponseEntity.ok(topicContentService.generateNotesForTopic(topicId));
    }
}
