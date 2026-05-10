package com.rayaan.ailearn.dto.response;

public record QuizSubmitResponse(
        Long attemptId,
        Double score,
        Double maxScore,
        String feedback
) {
}
