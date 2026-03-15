package com.rayaan.ailearn.dto.request;

import jakarta.validation.constraints.NotNull;

public record QuizGenerateRequest(
        @NotNull Long studentId,
        @NotNull Long topicId,
        Integer questionCount
) {
}
