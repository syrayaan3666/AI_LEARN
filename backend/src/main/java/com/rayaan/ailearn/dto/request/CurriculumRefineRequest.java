package com.rayaan.ailearn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CurriculumRefineRequest(
        @NotNull Long studentId,
        @NotNull Long curriculumId,
        @NotBlank String refinementPrompt
) {
}
