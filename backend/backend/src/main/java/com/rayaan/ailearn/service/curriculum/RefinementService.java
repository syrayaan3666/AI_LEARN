package com.rayaan.ailearn.service.curriculum;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rayaan.ailearn.dto.request.CurriculumRefineRequest;
import com.rayaan.ailearn.model.Curriculum;

@Service
public class RefinementService {

    private final CurricuForgeClient curricuForgeClient;
    private final ObjectMapper objectMapper;
    private final CurriculumNormalizerService curriculumNormalizerService;

    public RefinementService(
            CurricuForgeClient curricuForgeClient,
            ObjectMapper objectMapper,
            CurriculumNormalizerService curriculumNormalizerService
    ) {
        this.curricuForgeClient = curricuForgeClient;
        this.objectMapper = objectMapper;
        this.curriculumNormalizerService = curriculumNormalizerService;
    }

    public Map<String, Object> refine(Curriculum curriculum, CurriculumRefineRequest request) {
        try {
            Map<String, Object> existing = objectMapper.readValue(curriculum.getCurriculumJson(), new TypeReference<>() {});
            Map<String, Object> normalizedPlan = curriculumNormalizerService.normalize(existing);
            Map<String, Object> payload = Map.of(
                    "instruction", request.refinementPrompt(),
                    "current_plan", normalizedPlan
            );
            return curricuForgeClient.refine(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to refine curriculum", ex);
        }
    }
}
