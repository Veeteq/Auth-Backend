package com.veeteq.auth.authservice.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

public class UserAlreadyExistsException extends AbstractThrowableProblem {

    public UserAlreadyExistsException(String message) {
        super(URI.create("urn:user:not-found"),
                "User already exists",
                Status.CONFLICT,
                message);
    }
}