package com.rayaan.ailearn.repository;

import com.rayaan.ailearn.model.Quiz;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

	Optional<Quiz> findTopByUserIdAndTopicIdOrderByCreatedAtDesc(Long userId, Long topicId);
}
