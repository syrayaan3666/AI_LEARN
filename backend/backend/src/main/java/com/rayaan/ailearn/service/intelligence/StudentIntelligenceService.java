package com.rayaan.ailearn.service.intelligence;

import com.rayaan.ailearn.repository.StudentTopicProgressRepository;
import org.springframework.stereotype.Service;

@Service
public class StudentIntelligenceService {

    private final StudentTopicProgressRepository progressRepository;

    public StudentIntelligenceService(StudentTopicProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    public String buildProgressSummary(Long studentId, Long topicId) {
        return progressRepository.findByUserIdAndTopicId(studentId, topicId)
                .map(progress -> "Mastery score=" + Math.round(progress.getMasteryScore()) + ", status=" + progress.getStatus())
                .orElse("No prior progress found for this topic");
    }
}
