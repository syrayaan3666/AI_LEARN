package com.rayaan.ailearn.service.evaluation;

import org.springframework.stereotype.Service;

import com.rayaan.ailearn.model.Question;

@Service
public class MCQEvaluationService {

    public EvaluationResult evaluate(Question question, String answer) {
        String correct = question.getCorrectAnswer() == null ? "" : question.getCorrectAnswer().trim();
        String given = answer == null ? "" : answer.trim();
        boolean isCorrect = correct.equalsIgnoreCase(given);
        Double configuredPoints = question.getPoints();
        double points = configuredPoints != null ? configuredPoints : 1.0;
        double awarded = isCorrect ? points : 0.0;
        String feedback = isCorrect ? "Correct answer" : "Incorrect answer";
        return new EvaluationResult(isCorrect, awarded, feedback);
    }
}
