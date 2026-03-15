package com.rayaan.ailearn.dto.response;

import java.util.List;

public record QuizGenerateResponse(
        Long quizId,
        Long topicId,
        String difficulty,
        List<QuestionResponse> questions
) {
}
