package com.rayaan.ailearn.service.curriculum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rayaan.ailearn.dto.request.CurriculumGenerateRequest;
import com.rayaan.ailearn.dto.request.CurriculumRefineRequest;
import com.rayaan.ailearn.dto.response.CurriculumResponse;
import com.rayaan.ailearn.dto.response.TopicResponse;
import com.rayaan.ailearn.exception.BadRequestException;
import com.rayaan.ailearn.exception.ResourceNotFoundException;
import com.rayaan.ailearn.model.Curriculum;
import com.rayaan.ailearn.model.Topic;
import com.rayaan.ailearn.model.TopicContent;
import com.rayaan.ailearn.model.User;
import com.rayaan.ailearn.repository.CurriculumRepository;
import com.rayaan.ailearn.repository.TopicContentRepository;
import com.rayaan.ailearn.repository.TopicRepository;
import com.rayaan.ailearn.repository.UserRepository;

@Service
public class CurriculumOrchestrator {

    private final CurriculumRepository curriculumRepository;
    private final UserRepository userRepository;
    private final CurricuForgeClient curricuForgeClient;
    private final RefinementService refinementService;
    private final ObjectMapper objectMapper;
    private final TopicRepository topicRepository;
    private final TopicContentRepository topicContentRepository;
    private final CurriculumNormalizerService curriculumNormalizerService;

    public CurriculumOrchestrator(
            CurriculumRepository curriculumRepository,
            UserRepository userRepository,
            CurricuForgeClient curricuForgeClient,
            RefinementService refinementService,
            ObjectMapper objectMapper,
            TopicRepository topicRepository,
            TopicContentRepository topicContentRepository,
            CurriculumNormalizerService curriculumNormalizerService
    ) {
        this.curriculumRepository = curriculumRepository;
        this.userRepository = userRepository;
        this.curricuForgeClient = curricuForgeClient;
        this.refinementService = refinementService;
        this.objectMapper = objectMapper;
        this.topicRepository = topicRepository;
        this.topicContentRepository = topicContentRepository;
        this.curriculumNormalizerService = curriculumNormalizerService;
    }

    @Transactional
    public CurriculumResponse generate(CurriculumGenerateRequest request) {
        User user = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.studentId()));

        validatePlannerRequest(request);
        Map<String, Object> rawCurriculumJson = curricuForgeClient.generate(request);
        Map<String, Object> normalizedCurriculumJson = curriculumNormalizerService.normalize(rawCurriculumJson);

        String plannerType = normalizedPlannerType(request.plannerType());
        String subject = plannerType.equals("SEMESTER") ? request.skillDomain() : request.studyDomain();
        String learningLevel = plannerType.equals("SEMESTER") ? request.academicLevel() : request.experienceLevel();
        String duration = plannerType.equals("SEMESTER")
            ? String.valueOf(request.programDuration())
            : String.valueOf(request.duration());

        Curriculum saved = saveCurriculum(user, subject, learningLevel, duration, plannerType, normalizedCurriculumJson);
        return toResponse(saved);
    }

        @Transactional
        public CurriculumResponse generateMock(Long studentId) {
        User user = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        Map<String, Object> mockCurriculum = Map.of(
            "program_title", "Data Science Research Foundation",
            "summary", "A compact mock curriculum used for local testing when external generation is unavailable.",
            "semesters", List.of(
                Map.of(
                    "semester", 1,
                    "courses", List.of(
                        Map.of(
                            "title", "Introduction to Data Science",
                            "difficulty", "Beginner",
                            "skills", List.of("Python programming", "Data analysis", "Research basics"),
                            "topics", List.of(
                                Map.of("name", "Python for Data Science", "video_url", "https://www.youtube.com/results?search_query=Python+for+Data+Science+tutorial"),
                                Map.of("name", "Data Analysis with Pandas", "video_url", "https://www.youtube.com/results?search_query=Data+Analysis+with+Pandas+tutorial")
                            ),
                            "outcome_project", "Build a mini data analysis report from a CSV dataset"
                        ),
                        Map.of(
                            "title", "Statistics for Data Science",
                            "difficulty", "Beginner",
                            "skills", List.of("Probability", "Descriptive statistics", "Hypothesis testing"),
                            "topics", List.of(
                                Map.of("name", "Probability fundamentals", "video_url", "https://www.youtube.com/results?search_query=Probability+fundamentals+tutorial"),
                                Map.of("name", "Hypothesis testing", "video_url", "https://www.youtube.com/results?search_query=Hypothesis+testing+tutorial")
                            ),
                            "outcome_project", "Run and explain a simple hypothesis test"
                        )
                    )
                ),
                Map.of(
                    "semester", 2,
                    "courses", List.of(
                        Map.of(
                            "title", "Machine Learning Fundamentals",
                            "difficulty", "Intermediate",
                            "skills", List.of("Model training", "Evaluation", "Feature engineering"),
                            "topics", List.of(
                                Map.of("name", "Supervised learning", "video_url", "https://www.youtube.com/results?search_query=Supervised+learning+tutorial"),
                                Map.of("name", "Model evaluation metrics", "video_url", "https://www.youtube.com/results?search_query=Model+evaluation+metrics+tutorial")
                            ),
                            "outcome_project", "Train and evaluate a classification model"
                        ),
                        Map.of(
                            "title", "Research Methods in Data Science",
                            "difficulty", "Intermediate",
                            "skills", List.of("Experiment design", "Reproducibility", "Result communication"),
                            "topics", List.of(
                                Map.of("name", "A/B testing basics", "video_url", "https://www.youtube.com/results?search_query=A%2FB+testing+basics+tutorial"),
                                Map.of("name", "Experiment tracking", "video_url", "https://www.youtube.com/results?search_query=Experiment+tracking+tutorial")
                            ),
                            "outcome_project", "Write a concise experiment report with findings"
                        )
                    )
                )
            )
        );

        Curriculum saved = saveCurriculum(
            user,
            "Data Science",
            "Beginner",
            "2",
            "SEMESTER",
            mockCurriculum
        );
        return toResponse(saved);
        }

    @Transactional
    public CurriculumResponse refine(CurriculumRefineRequest request) {
        Curriculum curriculum = curriculumRepository.findById(request.curriculumId())
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found: " + request.curriculumId()));

        if (!curriculum.getUser().getId().equals(request.studentId())) {
            throw new ResourceNotFoundException("Curriculum does not belong to student");
        }

        Map<String, Object> refinedRaw = refinementService.refine(curriculum, request);
        Map<String, Object> normalizedRefined = curriculumNormalizerService.normalize(refinedRaw);
        curriculum.setCurriculumJson(writeJson(normalizedRefined));
        curriculum.setUpdatedAt(LocalDateTime.now());
        curriculumRepository.save(curriculum);
        return toResponse(curriculum);
    }

    public List<CurriculumResponse> getByStudent(Long studentId) {
        return curriculumRepository.findByUserIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CurriculumResponse getLatestByStudent(Long studentId) {
        return curriculumRepository.findByUserIdOrderByCreatedAtDesc(studentId)
                .stream()
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No curriculum found for student: " + studentId));
    }

    public Optional<CurriculumResponse> getLatestByStudentOptional(Long studentId) {
        return curriculumRepository.findByUserIdOrderByCreatedAtDesc(studentId)
                .stream()
                .findFirst()
                .map(this::toResponse);
    }

    private Curriculum saveCurriculum(
            User user,
            String subject,
            String learningLevel,
            String duration,
            String plannerType,
            Map<String, Object> curriculumJson
    ) {
        Curriculum entity = new Curriculum();
        entity.setUser(user);
        entity.setSubject(subject);
        entity.setLearningLevel(learningLevel);
        entity.setDuration(duration);
        entity.setPlannerType(plannerType);
        entity.setCurriculumJson(writeJson(curriculumJson));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return curriculumRepository.save(entity);
    }

    private CurriculumResponse toResponse(Curriculum curriculum) {
        try {
            Map<String, Object> raw = objectMapper.readValue(curriculum.getCurriculumJson(), new TypeReference<>() {});
            Map<String, Object> normalized = curriculumNormalizerService.normalize(raw);
            return new CurriculumResponse(
                    curriculum.getId(),
                    curriculum.getUser().getId(),
                    curriculum.getSubject(),
                    curriculum.getLearningLevel(),
                    curriculum.getDuration(),
                    curriculum.getPlannerType(),
                    normalized,
                    curriculum.getCreatedAt()
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to parse curriculum JSON", ex);
        }
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize curriculum JSON", ex);
        }
    }

    private void validatePlannerRequest(CurriculumGenerateRequest request) {
        String plannerType = normalizedPlannerType(request.plannerType());

        if (plannerType.equals("SEMESTER")) {
            requireText(request.skillDomain(), "skillDomain");
            requireText(request.academicLevel(), "academicLevel");
            requireNonNull(request.programDuration(), "programDuration");
            requireNonNull(request.weeklyHours(), "weeklyHours");
            requireText(request.focusArea(), "focusArea");
            requireNonNull(request.includeCapstone(), "includeCapstone");
            return;
        }

        if (plannerType.equals("PERSONAL")) {
            requireText(request.studyDomain(), "studyDomain");
            requireText(request.careerPath(), "careerPath");
            requireText(request.experienceLevel(), "experienceLevel");
            requireText(request.learningPace(), "learningPace");
            requireNonNull(request.weeklyHours(), "weeklyHours");
            requireNonNull(request.duration(), "duration");
            return;
        }

        throw new BadRequestException("plannerType must be either SEMESTER or PERSONAL");
    }

    private String normalizedPlannerType(String plannerType) {
        return plannerType == null ? "" : plannerType.trim().toUpperCase();
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required");
        }
    }

    private void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new BadRequestException(fieldName + " is required");
        }
    }

    @Transactional
    public List<TopicResponse> saveAsTopics(Long curriculumId, Long studentId, String curriculumName) {
        Curriculum curriculum = curriculumRepository.findById(curriculumId)
                .orElseThrow(() -> new ResourceNotFoundException("Curriculum not found: " + curriculumId));

        if (!curriculum.getUser().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Curriculum does not belong to student");
        }

        try {
            Map<String, Object> raw = objectMapper.readValue(curriculum.getCurriculumJson(), new TypeReference<>() {});
            Map<String, Object> normalized = curriculumNormalizerService.normalize(raw);

            List<ExtractedTopicData> extractedTopics = filterDuplicateTopics(extractTopicsFromJson(normalized, curriculumName));
            if (extractedTopics.isEmpty()) {
                return List.of();
            }

            List<Topic> topics = extractedTopics.stream().map(ExtractedTopicData::topic).toList();
            List<Topic> saved = topicRepository.saveAll(topics);
            seedTopicContentResources(saved, extractedTopics);
            return saved.stream()
                    .map(t -> new TopicResponse(
                            t.getId(),
                            t.getCurriculumName(),
                            t.getName(),
                            t.getDescription(),
                            t.getDifficulty(),
                            0.0,
                            "IN_PROGRESS"
                    ))
                    .toList();
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to parse curriculum JSON", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private List<ExtractedTopicData> extractTopicsFromJson(Map<String, Object> json, String curriculumName) {
        List<ExtractedTopicData> result = new ArrayList<>();
        Object semestersObj = json.get("semesters");
        if (semestersObj instanceof List<?> semesters) {
            for (Object semObj : semesters) {
                if (!(semObj instanceof Map<?, ?>)) continue;
                Map<String, Object> sem = (Map<String, Object>) semObj;
                Object coursesObj = sem.get("courses");
                if (!(coursesObj instanceof List<?>)) continue;

                for (Object courseObj : (List<?>) coursesObj) {
                    if (!(courseObj instanceof Map<?, ?>)) continue;
                    Map<String, Object> course = (Map<String, Object>) courseObj;
                    Topic topic = new Topic();
                    topic.setName(String.valueOf(course.getOrDefault("title", "Unnamed Course")));
                    topic.setCurriculumName(curriculumName);
                    Object skillsObj = course.get("skills");
                    if (skillsObj instanceof List<?> skills) {
                        topic.setDescription(skills.stream()
                                .map(Object::toString).collect(Collectors.joining(", ")));
                    }
                    topic.setDifficulty(String.valueOf(course.getOrDefault("difficulty", "Beginner")));
                    topic.setActive(true);
                    result.add(new ExtractedTopicData(topic, extractVideoLinks(course.get("topics"))));
                }
            }
            return result;
        }

        Object roadmapObj = json.get("roadmap");
        if (roadmapObj instanceof List<?> roadmap) {
            for (Object phaseObj : roadmap) {
                if (!(phaseObj instanceof Map<?, ?>)) continue;
                Map<String, Object> phase = (Map<String, Object>) phaseObj;
                Object milestonesObj = phase.get("milestones");
                if (!(milestonesObj instanceof List<?>)) continue;

                for (Object milestoneObj : (List<?>) milestonesObj) {
                    if (!(milestoneObj instanceof Map<?, ?>)) continue;
                    Map<String, Object> milestone = (Map<String, Object>) milestoneObj;

                    String milestoneTitle = String.valueOf(milestone.getOrDefault("title", "Unnamed Milestone"));
                    Object skillsObj = milestone.get("skills");
                    String description = "";
                    if (skillsObj instanceof List<?> skills && !skills.isEmpty()) {
                        description = skills.stream().map(Object::toString).collect(Collectors.joining(", "));
                    } else {
                        Object topicsObj = milestone.get("topics");
                        if (topicsObj instanceof List<?> topics && !topics.isEmpty()) {
                            List<String> names = new ArrayList<>();
                            for (Object topicObj : topics) {
                                if (topicObj instanceof Map<?, ?> topicMap) {
                                    Object name = topicMap.get("name");
                                    if (name != null) {
                                        names.add(String.valueOf(name));
                                    }
                                } else if (topicObj != null) {
                                    names.add(String.valueOf(topicObj));
                                }
                            }
                            description = String.join(", ", names);
                        }
                    }

                    String difficulty = "Beginner";
                    Object phaseNameObj = phase.get("phase");
                    if (phaseNameObj != null) {
                        String phaseName = String.valueOf(phaseNameObj).toLowerCase(Locale.ROOT);
                        if (phaseName.contains("advanced")) {
                            difficulty = "Intermediate";
                        }
                        if (phaseName.contains("mastery") || phaseName.contains("expert")) {
                            difficulty = "Advanced";
                        }
                    }

                    Topic topic = new Topic();
                    topic.setName(milestoneTitle);
                    topic.setCurriculumName(curriculumName);
                    topic.setDescription(description);
                    topic.setDifficulty(difficulty);
                    topic.setActive(true);
                    result.add(new ExtractedTopicData(topic, extractVideoLinks(milestone.get("topics"))));
                }
            }
        }

        return result;
    }

    private List<ExtractedTopicData> filterDuplicateTopics(List<ExtractedTopicData> topics) {
        Set<String> existingTopicKeys = topicRepository.findAll().stream()
                .map(this::topicKey)
                .filter(key -> !key.isBlank())
                .collect(Collectors.toCollection(HashSet::new));

        List<ExtractedTopicData> filtered = new ArrayList<>();
        Set<String> batchKeys = new HashSet<>();

        for (ExtractedTopicData item : topics) {
            Topic topic = item.topic();
            String key = topicKey(topic);
            if (key.isBlank() || existingTopicKeys.contains(key) || !batchKeys.add(key)) {
                continue;
            }
            filtered.add(item);
        }

        return filtered;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractVideoLinks(Object topicsObj) {
        if (!(topicsObj instanceof List<?> topics)) {
            return List.of();
        }

        List<String> links = new ArrayList<>();
        for (Object topicObj : topics) {
            if (!(topicObj instanceof Map<?, ?> topicMap)) {
                continue;
            }

            Object rawUrl = ((Map<String, Object>) topicMap).get("video_url");
            if (rawUrl == null || String.valueOf(rawUrl).isBlank()) {
                rawUrl = ((Map<String, Object>) topicMap).get("videoUrl");
            }

            if (rawUrl != null && !String.valueOf(rawUrl).isBlank()) {
                links.add(String.valueOf(rawUrl));
            }
        }

        return links.stream().distinct().toList();
    }

    private void seedTopicContentResources(List<Topic> savedTopics, List<ExtractedTopicData> extractedTopics) {
        Map<String, List<String>> linksByKey = extractedTopics.stream()
                .collect(Collectors.toMap(
                        item -> topicKey(item.topic()),
                        ExtractedTopicData::videoLinks,
                        (first, second) -> first
                ));

        List<TopicContent> contents = new ArrayList<>();
        for (Topic topic : savedTopics) {
            if (topicContentRepository.findByTopicId(topic.getId()).isPresent()) {
                continue;
            }

            List<String> links = linksByKey.getOrDefault(topicKey(topic), List.of());
            if (links.isEmpty()) {
                continue;
            }

            TopicContent content = new TopicContent();
            content.setTopic(topic);
            content.setNotes(topic.getDescription() == null || topic.getDescription().isBlank()
                    ? "currently nothing here"
                    : topic.getDescription());
            content.setResourcesJson(writeLinksJson(links));
            content.setUpdatedAt(LocalDateTime.now());
            contents.add(content);
        }

        if (!contents.isEmpty()) {
            topicContentRepository.saveAll(contents);
        }
    }

    private String writeLinksJson(List<String> links) {
        try {
            return objectMapper.writeValueAsString(links);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize topic video links", ex);
        }
    }

    private record ExtractedTopicData(Topic topic, List<String> videoLinks) {
    }

    private String topicKey(Topic topic) {
        String curriculum = topic.getCurriculumName();
        String raw = topic.getTitle();
        if (raw == null || raw.isBlank()) {
            raw = topic.getName();
        }
        String normalizedCurriculum = curriculum == null ? "" : curriculum.trim().toLowerCase(Locale.ROOT);
        String normalizedTopic = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return normalizedCurriculum + "::" + normalizedTopic;
    }
}
