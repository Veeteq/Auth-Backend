package com.veeteq.auth.authservice.exception;

import com.veeteq.auth.authservice.rest.dto.ProblemDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDto> handleBadCredentialsException(BadCredentialsException exc) {
        var response = new ProblemDto()
                .detail(exc.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> generic(RuntimeException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ProblemDto> handleUsernameNotFoundException(UsernameNotFoundException exc) {
        var response = new ProblemDto()
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(exc.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDto> handleAuthenticationException(AuthenticationException exc) {
        var response = new ProblemDto()
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(exc.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(response);
    }
}
