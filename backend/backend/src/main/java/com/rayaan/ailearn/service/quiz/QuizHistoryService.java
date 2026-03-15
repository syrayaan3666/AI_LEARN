package com.rayaan.ailearn.service.quiz;

import com.rayaan.ailearn.dto.response.QuizHistoryResponse;
import com.rayaan.ailearn.repository.QuizAttemptRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QuizHistoryService {

    private final QuizAttemptRepository quizAttemptRepository;

    public QuizHistoryService(QuizAttemptRepository quizAttemptRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public List<QuizHistoryResponse> getHistory(Long studentId) {
        return quizAttemptRepository.findByUserIdOrderByAttemptedAtDesc(studentId)
                .stream()
                .map(attempt -> new QuizHistoryResponse(
                        attempt.getId(),
                        attempt.getQuiz().getId(),
                        attempt.getQuiz().getTopic().getId(),
                        attempt.getQuiz().getTopic().getName(),
                        attempt.getScore(),
                        attempt.getMaxScore(),
                        attempt.getQuiz().getDifficulty(),
                    attempt.getAttemptedAt(),
                        attempt.getAttemptedAt()
                ))
                .toList();
    }
}
