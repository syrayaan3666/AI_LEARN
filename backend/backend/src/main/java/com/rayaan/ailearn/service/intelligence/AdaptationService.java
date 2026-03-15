package com.rayaan.ailearn.service.intelligence;

import org.springframework.stereotype.Service;

import com.rayaan.ailearn.repository.StudentTopicProgressRepository;

@Service
public class AdaptationService {

    private final StudentTopicProgressRepository progressRepository;

    public AdaptationService(StudentTopicProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    public String recommendDifficulty(Long studentId, Long topicId) {
        return progressRepository.findByUserIdAndTopicId(studentId, topicId)
                .map(progress -> {
                    Double masteryScore = progress.getMasteryScore();
                    double score = masteryScore != null ? masteryScore : 0.0;
                    if (score >= 80.0) {
                        return "HARD";
                    }
                    if (score >= 40.0) {
                        return "MEDIUM";
                    }
                    return "EASY";
                })
                .orElse("EASY");
    }
}
