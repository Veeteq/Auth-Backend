package com.veeteq.auth.authservice.exception;

import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Status;

import java.net.URI;

public class UserNotFoundException extends AbstractThrowableProblem {

    public UserNotFoundException(Long id) {
        super(URI.create("urn:user:not-found"),
                "User Not Found",
                Status.NOT_FOUND,
                "User not found: " + id);
    }

    public UserNotFoundException(String message) {
        super(URI.create("urn:user:not-found"),
                "User Not Found",
                Status.NOT_FOUND,
                message);
    }
}
