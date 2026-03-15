package com.rayaan.ailearn.dto.response;

public record TopicResponse(
        Long id,
        String curriculumName,
        String name,
        String description,
        String difficulty,
        Double progress,
        String status
) {
}
