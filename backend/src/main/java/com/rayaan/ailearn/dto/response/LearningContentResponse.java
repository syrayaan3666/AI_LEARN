package com.rayaan.ailearn.dto.response;

import java.util.List;
import java.util.Map;

public record LearningContentResponse(
        Long topicId,
        String topicTitle,
        String notes,
        List<VideoResourceResponse> videoLinks,
        Map<String, Object> notesJson
) {
}
