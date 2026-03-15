package com.rayaan.ailearn.service.curriculum;

import java.util.Map;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.rayaan.ailearn.dto.request.CurriculumGenerateRequest;
import com.rayaan.ailearn.exception.BadRequestException;

@Service
public class CurricuForgeClient {

    private static final Logger logger = LoggerFactory.getLogger(CurricuForgeClient.class);
    private final RestTemplate restTemplate;
    private final String generateUrl;
    private final String refineUrl;
    private final String generateMode;
    private final String refineMode;

    public CurricuForgeClient(
            RestTemplate restTemplate,
            @Value("${app.curricuforge.generate-url}") String generateUrl,
            @Value("${app.curricuforge.refine-url}") String refineUrl,
            @Value("${app.curricuforge.generate-mode:formatted}") String generateMode,
            @Value("${app.curricuforge.refine-mode:formatted}") String refineMode
    ) {
        this.restTemplate = restTemplate;
        this.generateUrl = generateUrl;
        this.refineUrl = refineUrl;
        this.generateMode = generateMode;
        this.refineMode = refineMode;
    }

    public Map<String, Object> generate(CurriculumGenerateRequest request) {
        Map<String, Object> payload = toExternalGeneratePayload(request);
        String resolvedGenerateUrl = resolveGenerateUrl();
        logger.info("Generating curriculum with planner type: {}", request.plannerType());
        logger.info("Sending payload to {}: {}", resolvedGenerateUrl, payload);
        try {
            return post(resolvedGenerateUrl, payload);
        } catch (BadRequestException ex) {
            logger.error("Bad request with payload: {}", payload, ex);
            // Fallback for older external deployments that still expect camelCase keys.
            if (ex.getMessage() != null && ex.getMessage().contains("500")) {
                return post(resolvedGenerateUrl, toLegacyExternalGeneratePayload(request));
            }
            throw ex;
        }
    }

    public Map<String, Object> refine(Map<String, Object> payload) {
        return post(resolveRefineUrl(), payload);
    }

    private String resolveGenerateUrl() {
        return resolveEndpointByMode(generateUrl, "/generate", generateMode);
    }

    private String resolveRefineUrl() {
        return resolveEndpointByMode(refineUrl, "/refine-plan", refineMode);
    }

    private String resolveEndpointByMode(String configuredUrl, String baseEndpoint, String mode) {
        if (configuredUrl == null || configuredUrl.isBlank()) {
            return configuredUrl;
        }

        String normalizedMode = mode == null ? "formatted" : mode.trim().toLowerCase();
        if (!configuredUrl.endsWith(baseEndpoint)) {
            return configuredUrl;
        }

        return switch (normalizedMode) {
            case "raw" -> configuredUrl.substring(0, configuredUrl.length() - baseEndpoint.length()) + baseEndpoint + "-raw";
            case "debug" -> configuredUrl.substring(0, configuredUrl.length() - baseEndpoint.length()) + baseEndpoint + "-debug";
            default -> configuredUrl;
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String url, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestClientException lastException = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                Map<String, Object> response = restTemplate.postForObject(url, new HttpEntity<>(payload, headers), Map.class);
                return response == null ? Map.of("semesters", java.util.List.of()) : response;
            } catch (RestClientException ex) {
                lastException = ex;
                String msg = ex.getMessage() != null ? ex.getMessage() : "unknown error";
                if (msg.contains("401")) {
                    throw new BadRequestException(
                            "Curriculum AI service is unavailable (authentication error). " +
                            "Please disable Vercel Authentication Protection on your CurricuForge deployment.");
                }
                if (!msg.contains("500") || attempt == 3) {
                    break;
                }
                LockSupport.parkNanos((400L * attempt) * 1_000_000L);
            }
        }
        String msg = lastException != null && lastException.getMessage() != null
                ? lastException.getMessage()
                : "unknown error";
        throw new BadRequestException("Curriculum AI service is unavailable: " + msg);
    }

    private Map<String, Object> toExternalGeneratePayload(CurriculumGenerateRequest request) {
        String plannerType = request.plannerType() == null ? "" : request.plannerType().trim().toLowerCase();

        if ("semester".equals(plannerType)) {
            return Map.of(
                    "planner_type", "semester",
                    "study_domain", nullSafeText(request.skillDomain()),
                    "career_path", nullSafeText(request.careerPath()),
                    "experience", mapAcademicLevelToExperience(request.academicLevel()),
                    "pace", nullSafeText(request.learningPace()),
                    "weekly_hours", nullSafeNumberText(request.weeklyHours()),
                    "duration", convertSemestersToDuration(request.programDuration())
            );
        }

        return Map.of(
                "planner_type", "personal",
                "study_domain", nullSafeText(request.studyDomain()),
                "career_path", nullSafeText(request.careerPath()),
                "experience", nullSafeText(request.experienceLevel()),
                "pace", nullSafeText(request.learningPace()),
                "weekly_hours", nullSafeNumberText(request.weeklyHours()),
                "duration", formatMonths(request.duration())
        );
    }

    private Map<String, Object> toLegacyExternalGeneratePayload(CurriculumGenerateRequest request) {
        String plannerType = request.plannerType() == null ? "" : request.plannerType().trim().toLowerCase();

        if ("semester".equals(plannerType)) {
            return Map.of(
                    "plannerType", "semester",
                    "skill", nullSafeText(request.skillDomain()),
                    "level", nullSafeText(request.academicLevel()),
                    "semesters", nullSafeNumberText(request.programDuration()),
                    "hours", nullSafeNumberText(request.weeklyHours()),
                    "focus", nullSafeText(request.focusArea()),
                    "includeCapstone", request.includeCapstone() != null && request.includeCapstone()
            );
        }

        return Map.of(
                "plannerType", "personal",
                "studyDomain", nullSafeText(request.studyDomain()),
                "careerPath", nullSafeText(request.careerPath()),
                "experience", nullSafeText(request.experienceLevel()),
                "pace", nullSafeText(request.learningPace()),
                "weeklyHours", nullSafeNumberText(request.weeklyHours()),
                "duration", formatMonths(request.duration())
        );
    }

    private String nullSafeText(String value) {
        return value == null ? "" : value;
    }

    private String nullSafeNumberText(Number value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String formatMonths(Integer months) {
        return months == null ? "" : months + " Months";
    }

    private String mapAcademicLevelToExperience(String academicLevel) {
        if (academicLevel == null) return "Beginner";
        return switch (academicLevel.trim().toLowerCase()) {
            case "bachelors" -> "Beginner";
            case "masters" -> "Intermediate";
            default -> "Beginner";
        };
    }

    private String convertSemestersToDuration(Integer semesters) {
        if (semesters == null) return "6 Months";
        // 4 semesters = 12 months, 6 semesters = 18 months, 8 semesters = 24 months
        int months = semesters * 3; // Approximate: 1 semester = 3 months
        return months + " Months";
    }
}
