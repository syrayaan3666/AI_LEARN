package com.rayaan.ailearn.service.quiz;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rayaan.ailearn.dto.request.QuizSubmitRequest;
import com.rayaan.ailearn.dto.response.QuizSubmitResponse;
import com.rayaan.ailearn.exception.BadRequestException;
import com.rayaan.ailearn.exception.ResourceNotFoundException;
import com.rayaan.ailearn.model.Question;
import com.rayaan.ailearn.model.Quiz;
import com.rayaan.ailearn.model.QuizAttempt;
import com.rayaan.ailearn.model.StudentAnswer;
import com.rayaan.ailearn.model.User;
import com.rayaan.ailearn.model.enums.QuestionType;
import com.rayaan.ailearn.repository.QuestionRepository;
import com.rayaan.ailearn.repository.QuizAttemptRepository;
import com.rayaan.ailearn.repository.QuizRepository;
import com.rayaan.ailearn.repository.StudentAnswerRepository;
import com.rayaan.ailearn.repository.UserRepository;
import com.rayaan.ailearn.service.evaluation.DescriptiveEvaluationService;
import com.rayaan.ailearn.service.evaluation.EvaluationResult;
import com.rayaan.ailearn.service.evaluation.MCQEvaluationService;
import com.rayaan.ailearn.service.evaluation.MasteryUpdateService;

@Service
public class QuizSubmissionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;
    private final MCQEvaluationService mcqEvaluationService;
    private final DescriptiveEvaluationService descriptiveEvaluationService;
    private final MasteryUpdateService masteryUpdateService;

    public QuizSubmissionService(
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            QuizAttemptRepository quizAttemptRepository,
            StudentAnswerRepository studentAnswerRepository,
            UserRepository userRepository,
            MCQEvaluationService mcqEvaluationService,
            DescriptiveEvaluationService descriptiveEvaluationService,
            MasteryUpdateService masteryUpdateService
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.userRepository = userRepository;
        this.mcqEvaluationService = mcqEvaluationService;
        this.descriptiveEvaluationService = descriptiveEvaluationService;
        this.masteryUpdateService = masteryUpdateService;
    }

    @Transactional
    public QuizSubmitResponse submit(QuizSubmitRequest request) {
        User user = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.studentId()));

        Quiz quiz = resolveQuiz(request, user.getId());

        if (!quiz.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Quiz does not belong to the student");
        }

        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        Map<Long, String> answerMap = new HashMap<>();
        request.answers().forEach(answer -> answerMap.put(answer.questionId(), answer.answer()));

        double score = 0.0;
        double maxScore = 0.0;

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setAttemptedAt(LocalDateTime.now());
        attempt.setEvaluationSummary("Evaluated successfully");
        attempt = quizAttemptRepository.save(attempt);

        for (Question question : questions) {
            String answer = answerMap.getOrDefault(question.getId(), "");
            EvaluationResult result = evaluate(question, answer);
            score += result.awardedScore();
            Double configuredPoints = question.getPoints();
            maxScore += configuredPoints != null ? configuredPoints : 1.0;

            StudentAnswer studentAnswer = new StudentAnswer();
            studentAnswer.setQuizAttempt(attempt);
            studentAnswer.setQuestion(question);
            studentAnswer.setAnswerText(answer);
            studentAnswer.setIsCorrect(result.correct());
            studentAnswer.setAwardedScore(result.awardedScore());
            studentAnswer.setFeedback(result.feedback());
            studentAnswerRepository.save(studentAnswer);
        }

        attempt.setScore(score);
        attempt.setMaxScore(maxScore);
        attempt.setEvaluationSummary("Score=" + score + "/" + maxScore);
        quizAttemptRepository.save(attempt);

        double ratio = maxScore == 0.0 ? 0.0 : score / maxScore;
        masteryUpdateService.updateMastery(user, quiz.getTopic(), ratio);

        return new QuizSubmitResponse(attempt.getId(), score, maxScore, "Quiz submitted and evaluated");
    }

    private Quiz resolveQuiz(QuizSubmitRequest request, Long studentId) {
        if (request.quizId() != null) {
            return quizRepository.findById(request.quizId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + request.quizId()));
        }

        if (request.topicId() == null) {
            throw new BadRequestException("Either quizId or topicId must be provided");
        }

        return quizRepository.findTopByUserIdAndTopicIdOrderByCreatedAtDesc(studentId, request.topicId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz not found for student " + studentId + " and topic " + request.topicId()
                ));
    }

    private EvaluationResult evaluate(Question question, String answer) {
        if (question.getQuestionType() == QuestionType.MCQ) {
            return mcqEvaluationService.evaluate(question, answer);
        }
        return descriptiveEvaluationService.evaluate(question, answer);
    }
}
