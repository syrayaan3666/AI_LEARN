package com.rayaan.ailearn.service.quiz;

import com.rayaan.ailearn.model.Topic;
import com.rayaan.ailearn.service.intelligence.StudentIntelligenceService;
import com.rayaan.ailearn.service.intelligence.TopicProgressionService;
import org.springframework.stereotype.Service;

@Service
public class QuizContextBuilder {

    private final StudentIntelligenceService studentIntelligenceService;
    private final TopicProgressionService topicProgressionService;

    public QuizContextBuilder(
            StudentIntelligenceService studentIntelligenceService,
            TopicProgressionService topicProgressionService
    ) {
        this.studentIntelligenceService = studentIntelligenceService;
        this.topicProgressionService = topicProgressionService;
    }

    public String buildContext(Long studentId, Topic topic) {
        String progress = studentIntelligenceService.buildProgressSummary(studentId, topic.getId());
        String hint = topicProgressionService.determineProgressionHint(studentId, topic.getId());
        return "Topic=" + topic.getName() + "; progress=" + progress + "; recommendation=" + hint;
    }
}
