package com.rayaan.ailearn.service.doubt;

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
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoubtAgentService {

    private final DoubtRepository doubtRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final ContextBuilderService contextBuilderService;
    private final LLMRouterService llmRouterService;

    public DoubtAgentService(
            DoubtRepository doubtRepository,
            UserRepository userRepository,
            TopicRepository topicRepository,
            ContextBuilderService contextBuilderService,
            LLMRouterService llmRouterService
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
        String aiAnswer = llmRouterService.ask(request.doubtText(), context);

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

    public List<DoubtHistoryResponse> getHistory(Long studentId) {
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
