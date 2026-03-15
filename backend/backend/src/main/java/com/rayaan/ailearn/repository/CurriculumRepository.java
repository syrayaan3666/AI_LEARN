package com.rayaan.ailearn.repository;

import com.rayaan.ailearn.model.Curriculum;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurriculumRepository extends JpaRepository<Curriculum, Long> {
    List<Curriculum> findByUserIdOrderByCreatedAtDesc(Long userId);
}
