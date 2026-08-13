package com.veeteq.auth.authservice.config;

import com.fasterxml.jackson.databind.Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.jackson.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@Configuration
public class JacksonProblemConfig {

    @Bean
    public Module problemModule() {
        return new ProblemModule();
    }

    @Bean
    public Module constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }
}
