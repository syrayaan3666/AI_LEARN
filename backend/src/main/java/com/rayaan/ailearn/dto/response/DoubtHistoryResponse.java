package com.rayaan.ailearn.dto.response;

import java.time.LocalDateTime;

public record DoubtHistoryResponse(
        Long doubtId,
        Long topicId,
        String doubtText,
        String question,
        String aiResponse,
        String answer,
        LocalDateTime createdAt
) {
}
