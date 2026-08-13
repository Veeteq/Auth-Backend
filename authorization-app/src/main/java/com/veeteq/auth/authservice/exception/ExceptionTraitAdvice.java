package com.veeteq.auth.authservice.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.zalando.problem.spring.web.advice.ProblemHandling;

@RestControllerAdvice
public class ExceptionTraitAdvice implements BadCredentialsExceptionTrait, MethodArgumentNotValidExceptionTrait, ProblemHandling {}
