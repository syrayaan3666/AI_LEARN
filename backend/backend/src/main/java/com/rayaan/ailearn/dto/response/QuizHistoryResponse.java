package com.rayaan.ailearn.dto.response;

import java.time.LocalDateTime;

public record QuizHistoryResponse(
        Long attemptId,
        Long quizId,
        Long topicId,
        String topic,
        Double score,
        Double maxScore,
        String difficulty,
        LocalDateTime dateAttempted,
        LocalDateTime attemptedAt
) {
}
