package com.veeteq.auth.authservice.service;

import java.time.Instant;
import java.util.List;

public record AccessTokenResult(
        String token,
        Instant expiresAt,
        List<String> roles
) {}
