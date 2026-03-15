package com.rayaan.ailearn.controller;

import com.rayaan.ailearn.dto.request.QuizGenerateRequest;
import com.rayaan.ailearn.dto.request.QuizSubmitRequest;
import com.rayaan.ailearn.dto.response.QuizGenerateResponse;
import com.rayaan.ailearn.dto.response.QuizHistoryResponse;
import com.rayaan.ailearn.dto.response.QuizSubmitResponse;
import com.rayaan.ailearn.service.quiz.QuizGenerationService;
import com.rayaan.ailearn.service.quiz.QuizHistoryService;
import com.rayaan.ailearn.service.quiz.QuizSubmissionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    private final QuizGenerationService quizGenerationService;
    private final QuizSubmissionService quizSubmissionService;
    private final QuizHistoryService quizHistoryService;

    public QuizController(
            QuizGenerationService quizGenerationService,
            QuizSubmissionService quizSubmissionService,
            QuizHistoryService quizHistoryService
    ) {
        this.quizGenerationService = quizGenerationService;
        this.quizSubmissionService = quizSubmissionService;
        this.quizHistoryService = quizHistoryService;
    }

    @PostMapping("/generate")
    public ResponseEntity<QuizGenerateResponse> generate(@Valid @RequestBody QuizGenerateRequest request) {
        return ResponseEntity.ok(quizGenerationService.generate(request));
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizSubmitResponse> submit(@Valid @RequestBody QuizSubmitRequest request) {
        return ResponseEntity.ok(quizSubmissionService.submit(request));
    }

    @GetMapping("/history/{studentId}")
    public ResponseEntity<List<QuizHistoryResponse>> history(@PathVariable Long studentId) {
        return ResponseEntity.ok(quizHistoryService.getHistory(studentId));
    }
}
