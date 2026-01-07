package com.veeteq.auth.authservice.security;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.entity.RefreshToken;
import com.veeteq.auth.authservice.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

	private final RefreshTokenRepository repository;
    private final SecureRandom random = new SecureRandom();

    private final int refreshDays;

	public RefreshTokenService(RefreshTokenRepository repository, @Value("${app.jwt.refresh-token-days:14}") int refreshDays) {
		this.repository = repository;
		this.refreshDays = refreshDays;
	}

    /** Create a new opaque token and persist it for the user. */
    @Transactional
    public RefreshToken issue(AuthUser authUser) {
        String token = generateOpaqueToken();
        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setAuthUser(authUser);
        rt.setCreatedAt(Instant.now());
        rt.setExpiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS));
        rt.setRevoked(false);
        return repository.save(rt);
    }

    /** Validate token (exists, not revoked, not expired). */
    @Transactional(readOnly = true)
    public RefreshToken validate(String token) {
        RefreshToken rt = repository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (rt.isRevoked()) throw new IllegalArgumentException("Refresh token revoked");
        if (rt.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Refresh token expired");
        return rt;
    }

    /** Rotate: revoke old token and issue new one for the same user. */
    @Transactional
    public RefreshToken rotate(RefreshToken old) {
        old.setRevoked(true);
        repository.save(old);
        return issue(old.getAuthUser());
    }

    /** Revoke (on logout). */
    @Transactional
    public void revoke(String token) {
        repository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            repository.save(rt);
        });
    }

	private String generateOpaqueToken() {
		byte[] bytes = new byte[64]; // 512 bits
        random.nextBytes(bytes);
        // URL-safe Base64; shorter tokens OK, but keep enough entropy
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

    
}
