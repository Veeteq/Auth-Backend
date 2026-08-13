package com.veeteq.auth.authservice.config;

import java.net.URI;

public final class ProblemTypes {
    private ProblemTypes() {}

    public static final URI BAD_CREDENTIALS = URI.create("urn:problem:auth:bad-credentials");
    public static final URI INVALID_ARGUMENT = URI.create("urn:problem:auth:invalid-argument");
    public static final URI NOT_FOUND = URI.create("urn:problem:auth:not-found");
    public static final URI CONFLICT = URI.create("urn:problem:auth:conflict");
}
