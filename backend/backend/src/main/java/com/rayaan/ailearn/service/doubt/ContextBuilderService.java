package com.rayaan.ailearn.service.doubt;

import com.rayaan.ailearn.model.Topic;
import com.rayaan.ailearn.repository.StudentTopicProgressRepository;
import com.rayaan.ailearn.repository.TopicContentRepository;
import com.rayaan.ailearn.service.intelligence.StudentIntelligenceService;
import org.springframework.stereotype.Service;

@Service
public class ContextBuilderService {

    private final TopicContentRepository topicContentRepository;
    private final StudentTopicProgressRepository progressRepository;
    private final StudentIntelligenceService studentIntelligenceService;

    public ContextBuilderService(
            TopicContentRepository topicContentRepository,
            StudentTopicProgressRepository progressRepository,
            StudentIntelligenceService studentIntelligenceService
    ) {
        this.topicContentRepository = topicContentRepository;
        this.progressRepository = progressRepository;
        this.studentIntelligenceService = studentIntelligenceService;
    }

    public String build(Long studentId, Topic topic, String doubtText) {
        String notes = topicContentRepository.findByTopicId(topic.getId())
                .map(content -> content.getNotes())
                .orElse("No notes available yet");

        String progress = progressRepository.findByUserIdAndTopicId(studentId, topic.getId())
                .map(value -> "mastery=" + value.getMasteryScore() + ", status=" + value.getStatus())
                .orElse("No progress available");

        String profile = studentIntelligenceService.buildProgressSummary(studentId, topic.getId());

        return "Topic: " + topic.getName() + "\n"
                + "Student profile: " + profile + "\n"
                + "Progress: " + progress + "\n"
                + "Notes: " + notes + "\n"
                + "Doubt: " + doubtText;
    }
}
