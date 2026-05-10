package com.rayaan.ailearn.service.intelligence;

import org.springframework.stereotype.Service;

import com.rayaan.ailearn.repository.StudentTopicProgressRepository;

@Service
public class TopicProgressionService {

    private final StudentTopicProgressRepository progressRepository;

    public TopicProgressionService(StudentTopicProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    public String determineProgressionHint(Long studentId, Long topicId) {
        return progressRepository.findByUserIdAndTopicId(studentId, topicId)
                .map(progress -> {
                    Double masteryScore = progress.getMasteryScore();
                    double mastery = masteryScore != null ? masteryScore : 0.0;
                    if (mastery >= 80.0) {
                        return "Move to advanced exercises";
                    }
                    if (mastery >= 40.0) {
                        return "Continue guided practice";
                    }
                    return "Revisit fundamentals";
                })
                .orElse("Start with beginner material");
    }
}
