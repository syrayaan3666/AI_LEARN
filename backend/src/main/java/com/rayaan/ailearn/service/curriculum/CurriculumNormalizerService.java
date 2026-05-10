package com.rayaan.ailearn.service.curriculum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class CurriculumNormalizerService {

    public Map<String, Object> normalize(Map<String, Object> rawPayload) {
        if (rawPayload == null || rawPayload.isEmpty()) {
            return Map.of("semesters", List.of());
        }

        Map<String, Object> plan = unwrapPlan(rawPayload);
        Map<String, Object> normalized = new LinkedHashMap<>();

        copyText(plan, normalized, "program_title", "programTitle", "title");
        copyText(plan, normalized, "summary", "description");

        List<Map<String, Object>> normalizedSemesters = normalizeSemesters(plan.get("semesters"));
        if (!normalizedSemesters.isEmpty()) {
            normalized.put("semesters", normalizedSemesters);
            return normalized;
        }

        List<Map<String, Object>> normalizedRoadmap = normalizeRoadmap(plan.get("roadmap"));
        if (!normalizedRoadmap.isEmpty()) {
            normalized.put("roadmap", normalizedRoadmap);
            return normalized;
        }

        return plan;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> unwrapPlan(Map<String, Object> rawPayload) {
        Object curriculum = rawPayload.get("curriculum");
        if (curriculum instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }

        Object data = rawPayload.get("data");
        if (data instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }

        Object plan = rawPayload.get("plan");
        if (plan instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }

        return rawPayload;
    }

    private void copyText(Map<String, Object> source, Map<String, Object> target, String... keys) {
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                target.put(keys[0], String.valueOf(value));
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeSemesters(Object semestersObj) {
        if (!(semestersObj instanceof List<?> semesters)) {
            return List.of();
        }

        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object semObj : semesters) {
            if (!(semObj instanceof Map<?, ?> semMap)) {
                continue;
            }

            Map<String, Object> semester = (Map<String, Object>) semMap;
            Map<String, Object> normalizedSemester = new LinkedHashMap<>();
            normalizedSemester.put("semester", toInt(semester.get("semester"), normalized.size() + 1));
            normalizedSemester.put("courses", normalizeCourses(semester.get("courses")));
            normalized.add(normalizedSemester);
        }

        normalized.sort(Comparator.comparingInt(s -> toInt(s.get("semester"), Integer.MAX_VALUE)));
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeCourses(Object coursesObj) {
        if (!(coursesObj instanceof List<?> courses)) {
            return List.of();
        }

        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object courseObj : courses) {
            if (!(courseObj instanceof Map<?, ?> courseMap)) {
                continue;
            }

            Map<String, Object> course = (Map<String, Object>) courseMap;
            Map<String, Object> normalizedCourse = new LinkedHashMap<>();
            normalizedCourse.put("title", text(course, "title", "name", "course_title", "courseName"));
            normalizedCourse.put("difficulty", defaultText(text(course, "difficulty", "level"), "Beginner"));
            normalizedCourse.put("skills", toTextList(course.get("skills")));
            List<Map<String, Object>> topics = normalizeTopicEntries(course.get("topics"));
            normalizedCourse.put("topics", topics);
            List<String> videoLinks = collectVideoLinks(topics);
            if (!videoLinks.isEmpty()) {
                normalizedCourse.put("video_links", videoLinks);
            }

            String outcome = text(course, "outcome_project", "outcomeProject", "project");
            if (!outcome.isBlank()) {
                normalizedCourse.put("outcome_project", outcome);
            }

            normalized.add(normalizedCourse);
        }
        return normalized;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeRoadmap(Object roadmapObj) {
        if (!(roadmapObj instanceof List<?> roadmap)) {
            return List.of();
        }

        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object phaseObj : roadmap) {
            if (!(phaseObj instanceof Map<?, ?> phaseMap)) {
                continue;
            }

            Map<String, Object> phase = (Map<String, Object>) phaseMap;
            Map<String, Object> normalizedPhase = new LinkedHashMap<>();
            normalizedPhase.put("phase", text(phase, "phase", "name", "title"));
            normalizedPhase.put("duration", text(phase, "duration", "timeline"));
            normalizedPhase.put("milestones", normalizeMilestones(phase.get("milestones")));
            normalized.add(normalizedPhase);
        }

        return normalized;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeMilestones(Object milestonesObj) {
        if (!(milestonesObj instanceof List<?> milestones)) {
            return List.of();
        }

        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object milestoneObj : milestones) {
            if (!(milestoneObj instanceof Map<?, ?> milestoneMap)) {
                continue;
            }

            Map<String, Object> milestone = (Map<String, Object>) milestoneMap;
            Map<String, Object> normalizedMilestone = new LinkedHashMap<>();
            normalizedMilestone.put("title", text(milestone, "title", "name", "milestone"));
            normalizedMilestone.put("skills", toTextList(milestone.get("skills")));
            List<Map<String, Object>> topics = normalizeTopicEntries(milestone.get("topics"));
            normalizedMilestone.put("topics", topics);
            List<String> videoLinks = collectVideoLinks(topics);
            if (!videoLinks.isEmpty()) {
                normalizedMilestone.put("video_links", videoLinks);
            }
            normalized.add(normalizedMilestone);
        }

        return normalized;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeTopicEntries(Object topicsObj) {
        if (!(topicsObj instanceof List<?> topics)) {
            return List.of();
        }

        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Object topicObj : topics) {
            if (topicObj instanceof Map<?, ?> topicMap) {
                Map<String, Object> topic = (Map<String, Object>) topicMap;
                Map<String, Object> normalizedTopic = new LinkedHashMap<>();
                normalizedTopic.put("name", text(topic, "name", "title", "topic"));
                String videoUrl = text(topic, "video_url", "videoUrl", "url", "link");
                if (!videoUrl.isBlank()) {
                    normalizedTopic.put("video_url", videoUrl);
                }
                normalized.add(normalizedTopic);
                continue;
            }

            if (topicObj != null) {
                normalized.add(Map.of("name", String.valueOf(topicObj)));
            }
        }
        return normalized;
    }

    private String text(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value).trim();
            }
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!keys[0].equals(entry.getKey()) && entry.getValue() instanceof String s && !s.isBlank()) {
                return s.trim();
            }
        }
        return "";
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @SuppressWarnings("unchecked")
    private List<String> toTextList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }

        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item == null) {
                continue;
            }

            if (item instanceof Map<?, ?> map) {
                String name = text((Map<String, Object>) map, "name", "title", "skill", "topic");
                if (!name.isBlank()) {
                    result.add(name);
                }
                continue;
            }

            String asText = String.valueOf(item).trim();
            if (!asText.isBlank()) {
                result.add(asText);
            }
        }
        return result;
    }

    private int toInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value).replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private List<String> collectVideoLinks(List<Map<String, Object>> topicEntries) {
        if (topicEntries == null || topicEntries.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> orderedUnique = new LinkedHashSet<>();
        for (Map<String, Object> topic : topicEntries) {
            Object rawLink = topic.get("video_url");
            if (rawLink != null) {
                String link = String.valueOf(rawLink).trim();
                if (!link.isBlank()) {
                    orderedUnique.add(link);
                }
            }
        }

        return new ArrayList<>(orderedUnique);
    }
}