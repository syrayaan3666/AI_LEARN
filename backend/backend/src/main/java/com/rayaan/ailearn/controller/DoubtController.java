package com.rayaan.ailearn.controller;

import com.rayaan.ailearn.dto.request.DoubtAskRequest;
import com.rayaan.ailearn.dto.response.DoubtAskResponse;
import com.rayaan.ailearn.dto.response.DoubtHistoryResponse;
import com.rayaan.ailearn.service.doubt.DoubtAgentService;
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
@RequestMapping("/doubts")
public class DoubtController {

    private final DoubtAgentService doubtAgentService;

    public DoubtController(DoubtAgentService doubtAgentService) {
        this.doubtAgentService = doubtAgentService;
    }

    @PostMapping("/ask")
    public ResponseEntity<DoubtAskResponse> ask(@Valid @RequestBody DoubtAskRequest request) {
        return ResponseEntity.ok(doubtAgentService.ask(request));
    }

    @GetMapping("/history/{studentId}")
    public ResponseEntity<List<DoubtHistoryResponse>> history(@PathVariable Long studentId) {
        return ResponseEntity.ok(doubtAgentService.getHistory(studentId));
    }
}
