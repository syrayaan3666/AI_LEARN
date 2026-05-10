package com.rayaan.ailearn.service.evaluation;

import org.springframework.stereotype.Service;

import com.rayaan.ailearn.model.Question;

@Service
public class DescriptiveEvaluationService {

    public EvaluationResult evaluate(Question question, String answer) {
        String content = answer == null ? "" : answer.trim();
        Double configuredPoints = question.getPoints();
        double points = configuredPoints != null ? configuredPoints : 2.0;
        if (content.isBlank()) {
            return new EvaluationResult(false, 0.0, "No descriptive answer provided");
        }

        double ratio;
        if (content.length() > 120) {
            ratio = 1.0;
        } else if (content.length() > 60) {
            ratio = 0.8;
        } else if (content.length() > 25) {
            ratio = 0.6;
        } else {
            ratio = 0.4;
        }

        double awarded = points * ratio;
        return new EvaluationResult(ratio >= 0.8, awarded, "Descriptive answer evaluated by heuristic scoring");
    }
}
