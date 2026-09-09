package com.veeteq.auth.authservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.AdviceTrait;

public interface AuthorizationDeniedExceptionTrait extends AdviceTrait {

    @ExceptionHandler
    default ResponseEntity<Problem> handleAuthorizationDeniedException(AuthorizationDeniedException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
                .withTitle("Access Denied")
                .withStatus(Status.FORBIDDEN)
                .withDetail("Access denied")
                .build();
        return create(ex, problem, request);
    }
}
/*
public interface AuthorizationDeniedExceptionTrait
        extends AdviceTrait {

    @ExceptionHandler
    default ResponseEntity<Problem>
    handleAuthorizationDeniedException(
            AuthorizationDeniedException ex,
            NativeWebRequest request) {

        Problem problem = Problem.builder()
                .withTitle("Access Denied")
                .withStatus(Status.FORBIDDEN)
                .withDetail("Access denied")
                .build();

        return create(ex, problem, request);
    }
}
 */
