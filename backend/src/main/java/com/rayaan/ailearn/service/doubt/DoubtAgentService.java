package com.rayaan.ailearn.service.doubt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rayaan.ailearn.dto.request.DoubtAskRequest;
import com.rayaan.ailearn.dto.response.DoubtAskResponse;
import com.rayaan.ailearn.dto.response.DoubtHistoryResponse;
import com.rayaan.ailearn.exception.ResourceNotFoundException;
import com.rayaan.ailearn.model.Doubt;
import com.rayaan.ailearn.model.Topic;
import com.rayaan.ailearn.model.User;
import com.rayaan.ailearn.repository.DoubtRepository;
import com.rayaan.ailearn.repository.TopicRepository;
import com.rayaan.ailearn.repository.UserRepository;
import com.rayaan.ailearn.service.intelligence.LLMRouterService;

@Service
public class DoubtAgentService {

    private final DoubtRepository doubtRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final ContextBuilderService contextBuilderService;
    private final Optional<LLMRouterService> llmRouterService;

    public DoubtAgentService(
            DoubtRepository doubtRepository,
            UserRepository userRepository,
            TopicRepository topicRepository,
            ContextBuilderService contextBuilderService,
            Optional<LLMRouterService> llmRouterService
    ) {
        this.doubtRepository = doubtRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.contextBuilderService = contextBuilderService;
        this.llmRouterService = llmRouterService;
    }

    @Transactional
    public DoubtAskResponse ask(DoubtAskRequest request) {
        User user = userRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.studentId()));
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + request.topicId()));

        String context = contextBuilderService.build(user.getId(), topic, request.doubtText());
        String aiAnswer = llmRouterService.isPresent() 
            ? llmRouterService.get().ask(request.doubtText(), context)
            : generateFallbackAnswer(topic, request.doubtText());

        Doubt doubt = new Doubt();
        doubt.setUser(user);
        doubt.setTopic(topic);
        doubt.setDoubtText(request.doubtText());
        doubt.setAiResponse(aiAnswer);
        doubt.setContextSnapshot(context);
        doubt.setCreatedAt(LocalDateTime.now());
        doubt = doubtRepository.save(doubt);

        return new DoubtAskResponse(doubt.getId(), aiAnswer);
    }
    
    private String generateFallbackAnswer(Topic topic, String doubtText) {
        return "Based on the topic '" + topic.getName() + "', here's a response about your doubt:\n\n"
                + "Your doubt: " + doubtText + "\n\n"
                + "To get a more detailed AI-powered answer, please ensure the LLM service is configured.";
    }

    public List<DoubtHistoryResponse> getHistory(Long studentId) {
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        return doubtRepository.findByUserIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(doubt -> new DoubtHistoryResponse(
                        doubt.getId(),
                        doubt.getTopic().getId(),
                        doubt.getDoubtText(),
                        doubt.getDoubtText(),
                        doubt.getAiResponse(),
                        doubt.getAiResponse(),
                        doubt.getCreatedAt()
                ))
                .toList();
    }
}
