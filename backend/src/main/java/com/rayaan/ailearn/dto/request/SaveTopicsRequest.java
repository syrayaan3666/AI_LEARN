package com.rayaan.ailearn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveTopicsRequest(
	@NotNull Long studentId,
	@NotBlank String curriculumName
) {
}
