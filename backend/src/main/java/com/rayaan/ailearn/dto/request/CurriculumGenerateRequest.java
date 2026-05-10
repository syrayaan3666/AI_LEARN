package com.rayaan.ailearn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CurriculumGenerateRequest(
        @NotNull Long studentId,
        @NotBlank String plannerType,

        // Semester planner fields
        String skillDomain,
        String academicLevel,
        Integer programDuration,
        Integer weeklyHours,
        String focusArea,
        String careerPath,
        String learningPace,
        Boolean includeCapstone,

        // Personal planner fields
        String studyDomain,
        String experienceLevel,
        Integer duration
) {
}
