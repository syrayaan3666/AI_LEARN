package com.rayaan.ailearn.repository;

import com.rayaan.ailearn.model.Topic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByActiveTrue();
}
