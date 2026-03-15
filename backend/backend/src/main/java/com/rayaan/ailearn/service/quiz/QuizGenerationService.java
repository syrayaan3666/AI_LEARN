package com.rayaan.ailearn.service.quiz;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rayaan.ailearn.dto.request.QuizGenerateRequest;
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

@Service
public class QuizGenerationService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final AdaptationService adaptationService;
    private final QuizContextBuilder quizContextBuilder;
    private final ObjectMapper objectMapper;

    public QuizGenerationService(
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            TopicRepository topicRepository,
            UserRepository userRepository,
            AdaptationService adaptationService,
            QuizContextBuilder quizContextBuilder,
            ObjectMapper objectMapper
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.adaptationService = adaptationService;
        this.quizContextBuilder = quizContextBuilder;
        this.objectMapper = objectMapper;
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
