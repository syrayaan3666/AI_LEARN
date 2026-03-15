package com.rayaan.ailearn.repository;

import com.rayaan.ailearn.model.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {
}
