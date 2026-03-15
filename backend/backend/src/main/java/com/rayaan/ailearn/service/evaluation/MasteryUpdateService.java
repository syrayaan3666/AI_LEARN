package com.rayaan.ailearn.service.evaluation;

import com.rayaan.ailearn.model.StudentTopicProgress;
import com.rayaan.ailearn.model.Topic;
import com.rayaan.ailearn.model.User;
import com.rayaan.ailearn.model.enums.MasteryStatus;
import com.rayaan.ailearn.repository.StudentTopicProgressRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class MasteryUpdateService {

    private final StudentTopicProgressRepository progressRepository;

    public MasteryUpdateService(StudentTopicProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    public void updateMastery(User user, Topic topic, double scoreRatio) {
        StudentTopicProgress progress = progressRepository.findByUserIdAndTopicId(user.getId(), topic.getId())
                .orElseGet(StudentTopicProgress::new);

        progress.setUser(user);
        progress.setTopic(topic);
        progress.setMasteryScore(scoreRatio * 100.0);
        progress.setStatus(resolveStatus(scoreRatio));
        progress.setUpdatedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    private MasteryStatus resolveStatus(double ratio) {
        if (ratio >= 0.8) {
            return MasteryStatus.MASTERED;
        }
        if (ratio >= 0.4) {
            return MasteryStatus.IN_PROGRESS;
        }
        return MasteryStatus.NEW;
    }
}
