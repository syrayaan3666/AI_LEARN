package com.rayaan.ailearn.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataBootstrapConfig {

    @Bean
    public CommandLineRunner seedDefaultStudent(JdbcTemplate jdbcTemplate) {
        return args -> jdbcTemplate.update(
                "INSERT INTO users (id, name, email, password) VALUES (1, 'Demo Student', 'student1@ailearn.local', 'local-dev-only') "
                        + "ON CONFLICT (id) DO NOTHING");
    }
}
