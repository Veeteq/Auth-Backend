package com.veeteq.auth.authservice.exception;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.AdviceTrait;

import java.net.URI;

import static com.veeteq.auth.authservice.config.ProblemTypes.BAD_CREDENTIALS;
import static org.zalando.problem.Status.UNAUTHORIZED;

public interface BadCredentialsExceptionTrait extends AdviceTrait {

    @ExceptionHandler
    default ResponseEntity<Problem> handleBadCredentialsException(final BadCredentialsException e, final NativeWebRequest request) {
        // Create a custom Problem response
        var rootCause = Problem.builder()
                .withTitle(e.getMessage())
                .withStatus(UNAUTHORIZED)
                .build();
        var problem = Problem.builder()
                .withCause(rootCause)
                .withType(BAD_CREDENTIALS) // Custom URI for the problem type
                .withTitle("Invalid Credentials") // Title of the problem
                .withStatus(Status.UNAUTHORIZED) // HTTP status
                .withDetail("The provided credentials are invalid.") // Detailed message
                .with("username", e.getMessage()) // Add any additional parameters
                .build();
        return ResponseEntity.status(Status.UNAUTHORIZED.getStatusCode())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

}
