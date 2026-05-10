package com.rayaan.ailearn.repository;

import com.rayaan.ailearn.model.StudentTopicProgress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentTopicProgressRepository extends JpaRepository<StudentTopicProgress, Long> {
    Optional<StudentTopicProgress> findByUserIdAndTopicId(Long userId, Long topicId);

    List<StudentTopicProgress> findByUserId(Long userId);
}
