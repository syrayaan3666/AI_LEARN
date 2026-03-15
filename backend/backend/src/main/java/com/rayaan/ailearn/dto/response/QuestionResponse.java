package com.rayaan.ailearn.dto.response;

import java.util.List;

public record QuestionResponse(
        Long id,
        String type,
        String question,
        List<String> options
) {
}
