package com.rayaan.ailearn.service.doubt;

import org.springframework.stereotype.Service;

import com.rayaan.ailearn.model.Topic;
import com.rayaan.ailearn.repository.StudentTopicProgressRepository;
import com.rayaan.ailearn.repository.TopicContentRepository;
import com.rayaan.ailearn.service.intelligence.StudentIntelligenceService;
import com.rayaan.ailearn.service.learning.TopicContentService;

@Service
public class ContextBuilderService {

    private final TopicContentRepository topicContentRepository;
    private final StudentTopicProgressRepository progressRepository;
    private final StudentIntelligenceService studentIntelligenceService;
    private final TopicContentService topicContentService;

    public ContextBuilderService(
            TopicContentRepository topicContentRepository,
            StudentTopicProgressRepository progressRepository,
            StudentIntelligenceService studentIntelligenceService,
            TopicContentService topicContentService
    ) {
        this.topicContentRepository = topicContentRepository;
        this.progressRepository = progressRepository;
        this.studentIntelligenceService = studentIntelligenceService;
        this.topicContentService = topicContentService;
    }

    public String build(Long studentId, Topic topic, String doubtText) {
        String notes = topicContentRepository.findByTopicId(topic.getId())
                .map(content -> content.getNotes())
                .orElseGet(() -> {
                    try {
                        var generated = topicContentService.generateNotesForTopic(topic.getId());
                        return generated.notes() != null ? generated.notes() : "Notes generation in progress";
                    } catch (Exception e) {
                        return "Unable to generate notes at this time";
                    }
                });

        String progress = progressRepository.findByUserIdAndTopicId(studentId, topic.getId())
                .map(value -> "mastery=" + value.getMasteryScore() + ", status=" + value.getStatus())
                .orElse("No progress available");

        String profile = studentIntelligenceService.buildProgressSummary(studentId, topic.getId());

        String context = "Topic Name: " + topic.getName() + "\n"
                + "Topic Description: " + (topic.getDescription() != null ? topic.getDescription() : "No description") + "\n"
                + "Student profile: " + profile + "\n"
                + "Progress: " + progress + "\n"
                + "Notes: " + notes + "\n"
                + "Doubt: " + doubtText;
        
        return context;
    }
}
