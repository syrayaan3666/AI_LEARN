package com.rayaan.ailearn.dto.request;

import jakarta.validation.constraints.NotNull;

public record CurriculumMockGenerateRequest(
        @NotNull Long studentId
) {
}
