package com.rayaan.ailearn.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rayaan.ailearn.dto.request.CurriculumGenerateRequest;
import com.rayaan.ailearn.dto.request.CurriculumMockGenerateRequest;
import com.rayaan.ailearn.dto.request.CurriculumRefineRequest;
import com.rayaan.ailearn.dto.request.SaveTopicsRequest;
import com.rayaan.ailearn.dto.response.CurriculumResponse;
import com.rayaan.ailearn.dto.response.TopicResponse;
import com.rayaan.ailearn.service.curriculum.CurriculumOrchestrator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/curriculum")
public class CurriculumController {

    private final CurriculumOrchestrator curriculumOrchestrator;

    public CurriculumController(CurriculumOrchestrator curriculumOrchestrator) {
        this.curriculumOrchestrator = curriculumOrchestrator;
    }

    @PostMapping("/generate")
    public ResponseEntity<CurriculumResponse> generate(@Valid @RequestBody CurriculumGenerateRequest request) {
        return ResponseEntity.ok(curriculumOrchestrator.generate(request));
    }

    @PostMapping("/generate-mock")
    public ResponseEntity<CurriculumResponse> generateMock(@Valid @RequestBody CurriculumMockGenerateRequest request) {
        return ResponseEntity.ok(curriculumOrchestrator.generateMock(request.studentId()));
    }

    @PostMapping("/refine")
    public ResponseEntity<CurriculumResponse> refine(@Valid @RequestBody CurriculumRefineRequest request) {
        return ResponseEntity.ok(curriculumOrchestrator.refine(request));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<CurriculumResponse> getByStudent(@PathVariable Long studentId) {
        return curriculumOrchestrator.getLatestByStudentOptional(studentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{curriculumId}/save-topics")
    public ResponseEntity<List<TopicResponse>> saveAsTopics(
            @PathVariable Long curriculumId,
            @Valid @RequestBody SaveTopicsRequest request) {
        return ResponseEntity.ok(
                curriculumOrchestrator.saveAsTopics(curriculumId, request.studentId(), request.curriculumName())
        );
    }
}
