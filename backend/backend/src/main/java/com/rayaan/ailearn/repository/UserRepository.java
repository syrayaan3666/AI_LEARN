package com.rayaan.ailearn.repository;

import com.rayaan.ailearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
