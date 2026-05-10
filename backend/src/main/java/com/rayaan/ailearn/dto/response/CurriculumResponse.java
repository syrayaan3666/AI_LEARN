package com.rayaan.ailearn.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record CurriculumResponse(
        Long curriculumId,
        Long studentId,
        String subject,
        String learningLevel,
        String duration,
        String plannerType,
        Map<String, Object> curriculum,
        LocalDateTime createdAt
) {
}
