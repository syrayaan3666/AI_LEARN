package com.rayaan.ailearn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DoubtAskRequest(
        @NotNull Long studentId,
        @NotNull Long topicId,
        @NotBlank String doubtText
) {
}
