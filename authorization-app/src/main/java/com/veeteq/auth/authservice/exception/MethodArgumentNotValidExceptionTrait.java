package com.veeteq.auth.authservice.exception;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.validation.MethodArgumentNotValidAdviceTrait;

import java.net.URI;
import java.util.Map;

import static com.veeteq.auth.authservice.config.ProblemTypes.INVALID_ARGUMENT;
import static org.zalando.problem.Status.BAD_REQUEST;

public interface MethodArgumentNotValidExceptionTrait extends MethodArgumentNotValidAdviceTrait {

    @Override
    default ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException exc, NativeWebRequest request) {
        // Create a custom Problem response
        var violations = exc.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        Map.of(
                                "field", error.getField(),
                                "message", error.getDefaultMessage()))
                .toList();
        var rootCause = Problem.builder()
                .withTitle(exc.getMessage())
                .withStatus(BAD_REQUEST)
                .build();
        var problem = Problem.builder()
                //.withCause(rootCause)
                .withType(INVALID_ARGUMENT) // Custom URI for the problem type
                .withTitle("Argument Not Valid") // Title of the problem
                .withStatus(BAD_REQUEST) // HTTP status
                .withDetail("The argument provided to the method is invalid.") // Detailed message
                .with("violations", violations)
                .build();
        return ResponseEntity.status(BAD_REQUEST.getStatusCode())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

}