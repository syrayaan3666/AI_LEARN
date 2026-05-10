package com.rayaan.ailearn.service.quiz;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rayaan.ailearn.dto.request.QuizGenerateRequest;
import com.rayaan.ailearn.dto.response.LearningContentResponse;
import com.rayaan.ailearn.dto.response.QuestionResponse;
import com.rayaan.ailearn.dto.response.QuizGenerateResponse;
import com.rayaan.ailearn.exception.ResourceNotFoundException;
import com.rayaan.ailearn.model.Question;
import com.rayaan.ailearn.model.Quiz;
import com.rayaan.ailearn.model.Topic;
import com.rayaan.ailearn.model.User;
import com.rayaan.ailearn.model.enums.QuestionType;
import com.rayaan.ailearn.repository.QuestionRepository;
import com.rayaan.ailearn.repository.QuizRepository;
import com.rayaan.ailearn.repository.TopicRepository;
import com.rayaan.ailearn.repository.UserRepository;
import com.rayaan.ailearn.service.intelligence.AdaptationService;
import com.rayaan.ailearn.service.intelligence.LLMRouterService;
import com.rayaan.ailearn.service.learning.TopicContentService;

@Service
public class QuizGenerationService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final AdaptationService adaptationService;
    private final QuizContextBuilder quizContextBuilder;
    private final ObjectMapper objectMapper;
    private final Optional<LLMRouterService> llmRouterService;
    private final Optional<TopicContentService> topicContentService;

    public QuizGenerationService(
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            TopicRepository topicRepository,
            UserRepository userRepository,
            AdaptationService adaptationService,
            QuizContextBuilder quizContextBuilder,
            ObjectMapper objectMapper,
            Optional<LLMRouterService> llmRouterService,
            Optional<TopicContentService> topicContentService
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.adaptationService = adaptationService;
        this.quizContextBuilder = quizContextBuilder;
        this.objectMapper = objectMapper;
        this.llmRouterService = llmRouterService;
        this.topicContentService = topicContentService;
    }

    @Transactional
    public QuizGenerateResponse generate(QuizGenerateRequest request) {
        User user = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.studentId()));
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.topicId()));

        Integer requestedCount = request.questionCount();
        int count = 5;
        if (requestedCount != null && requestedCount >= 2) {
            count = requestedCount;
        }

        // Prefer agent-based generation when LLM router is available
        if (llmRouterService.isPresent() && topicContentService.isPresent()) {
            try {
                return generateViaAgent(user, topic, count);
            } catch (Exception ex) {
                // fallback to template generation on any failure
            }
        }

        String difficulty = adaptationService.recommendDifficulty(user.getId(), topic.getId());
        String context = quizContextBuilder.buildContext(user.getId(), topic);

        Quiz quiz = new Quiz();
        quiz.setUser(user);
        quiz.setTopic(topic);
        quiz.setDifficulty(difficulty);
        quiz.setTotalQuestions(count);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz = quizRepository.save(quiz);

        List<Question> questions = buildQuestionSet(quiz, topic.getName(), context, count);
        questionRepository.saveAll(questions);

        return new QuizGenerateResponse(
                quiz.getId(),
                topic.getId(),
                difficulty,
                questions.stream().map(this::toQuestionResponse).toList()
        );
    }

    private QuizGenerateResponse generateViaAgent(User user, Topic topic, int count) throws Exception {
        LearningContentResponse content = topicContentService.get().getLearningContent(topic.getId());

        Map<String, Object> contextMap = new java.util.LinkedHashMap<>();
        Map<String, Object> topicMap = new java.util.LinkedHashMap<>();
        topicMap.put("id", topic.getId());
        topicMap.put("name", topic.getName());
        topicMap.put("description", topic.getDescription());
        contextMap.put("topic", topicMap);
        contextMap.put("notes", content.notesJson());
        contextMap.put("readable_notes", content.notes());
        contextMap.put("video_links", content.videoLinks());

        String contextJson = objectMapper.writeValueAsString(contextMap);

        String prompt = "Generate a quiz for the given topic. Output must be strictly valid JSON with a top-level 'questions' array. Each question item must contain: 'type' (\"MCQ\" or \"DESCRIPTIVE\"), 'question' (string), 'options' (array of strings, required for MCQ), 'answer' (string with the correct answer), and 'points' (number). Produce exactly " + count + " questions. Ensure answers match options for MCQ. Do not include markdown or explanatory text outside the JSON. The context JSON is provided separately.";

            String llmResponse = llmRouterService
                .orElseThrow(() -> new IllegalStateException("LLM service not available"))
                .ask(prompt, contextJson);

        // Parse LLM response as JSON
            Map<String, Object> parsed = objectMapper.readValue(llmResponse,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                });
        Object rawQuestions = parsed.get("questions");
        if (!(rawQuestions instanceof List<?> qList) || qList.isEmpty()) {
            throw new IllegalStateException("LLM did not return valid questions array");
        }

        String difficulty = adaptationService.recommendDifficulty(user.getId(), topic.getId());

        Quiz quiz = new Quiz();
        quiz.setUser(user);
        quiz.setTopic(topic);
        quiz.setDifficulty(difficulty);
        quiz.setTotalQuestions(count);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz = quizRepository.save(quiz);

        List<Question> created = new ArrayList<>();
        for (Object obj : qList) {
            if (!(obj instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> qm = (Map<String, Object>) obj;
            
            String type = String.valueOf(qm.getOrDefault("type", "DESCRIPTIVE")).toUpperCase();
            String qtext = String.valueOf(qm.getOrDefault("question", ""));
            Object optsObj = qm.get("options");
            List<String> options = new ArrayList<>();
            if (optsObj instanceof List<?> ol) {
                for (Object o : ol) options.add(String.valueOf(o));
            }
            String answer = String.valueOf(qm.getOrDefault("answer", ""));
            double points = 1.0;
            try {
                String rawPoints = String.valueOf(qm.getOrDefault("points", "1.0"));
                points = Double.parseDouble(rawPoints);
            } catch (NumberFormatException ignored) {
                // leave default points
            }

            Question q = new Question();
            q.setQuiz(quiz);
            q.setQuestionText(qtext);
            q.setPoints(points);
            if ("MCQ".equals(type)) {
                q.setQuestionType(QuestionType.MCQ);
                try {
                    q.setOptionsJson(objectMapper.writeValueAsString(options));
                } catch (JsonProcessingException e) {
                    q.setOptionsJson("[]");
                }
                q.setCorrectAnswer(answer);
            } else {
                q.setQuestionType(QuestionType.DESCRIPTIVE);
                q.setOptionsJson("[]");
                q.setCorrectAnswer(answer);
            }
            created.add(q);
        }

        questionRepository.saveAll(created);

        List<QuestionResponse> responses = created.stream().map(this::toQuestionResponse).toList();
        return new QuizGenerateResponse(quiz.getId(), topic.getId(), difficulty, responses);
    }

    private List<Question> buildQuestionSet(Quiz quiz, String topicName, String context, int count) {
        List<Question> questions = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Question q = new Question();
            q.setQuiz(quiz);
            q.setPoints(i % 2 == 0 ? 2.0 : 1.0);

            if (i % 2 == 0) {
                q.setQuestionType(QuestionType.DESCRIPTIVE);
                q.setQuestionText("Explain concept " + i + " for " + topicName + " using this context: " + context);
                q.setCorrectAnswer("Open-ended");
                q.setOptionsJson("[]");
            } else {
                q.setQuestionType(QuestionType.MCQ);
                q.setQuestionText("Which statement is most accurate about " + topicName + "? (Q" + i + ")");
                List<String> options = List.of("Core concept", "Irrelevant concept", "Deprecated concept", "None");
                q.setCorrectAnswer("Core concept");
                q.setOptionsJson(toJson(options));
            }
            questions.add(q);
        }
        return questions;
    }

    private QuestionResponse toQuestionResponse(Question q) {
        try {
            List<String> options = objectMapper.readValue(q.getOptionsJson(), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            return new QuestionResponse(q.getId(), q.getQuestionType().name(), q.getQuestionText(), options);
        } catch (JsonProcessingException ex) {
            return new QuestionResponse(q.getId(), q.getQuestionType().name(), q.getQuestionText(), List.of());
        }
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }
}

