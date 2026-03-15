package com.rayaan.ailearn.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record QuizSubmitRequest(
        @NotNull Long studentId,
        Long quizId,
        Long topicId,
        @NotEmpty List<AnswerPayload> answers
) {
    public record AnswerPayload(
            @NotNull Long questionId,
            @NotNull String answer
    ) {
    }
}
