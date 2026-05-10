package com.rayaan.ailearn.service.evaluation;

public record EvaluationResult(boolean correct, double awardedScore, String feedback) {
}
