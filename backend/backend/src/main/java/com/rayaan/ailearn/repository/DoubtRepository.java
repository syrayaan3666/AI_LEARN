package com.rayaan.ailearn.repository;

import com.rayaan.ailearn.model.Doubt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoubtRepository extends JpaRepository<Doubt, Long> {
    List<Doubt> findByUserIdOrderByCreatedAtDesc(Long userId);
}
