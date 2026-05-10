package com.rayaan.ailearn.repository;

import com.rayaan.ailearn.model.TopicContent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicContentRepository extends JpaRepository<TopicContent, Long> {
    Optional<TopicContent> findByTopicId(Long topicId);
}
