package com.veeteq.auth.authservice.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens",
        indexes = { @Index(name = "idx_refresh_token_token", columnList = "token", unique = true) })

public class RefreshToken {

    @Id
    @Column(name = "token_id")
    private Long id;

    /** Opaque random token (consider hashing in DB for extra safety). */
    @Column(name = "token", nullable = false, length = 512, unique = true)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "refresh_token_user_fk"))
    private AuthUser authUser;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "create_time")
    @CreatedDate
    private Instant createdAt;

    @Column(name = "update_time")
    @LastModifiedDate
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public RefreshToken setId(Long id) {
        this.id = id;
        return this;
    }

    public String getToken() {
        return token;
    }

    public RefreshToken setToken(String token) {
        this.token = token;
        return this;
    }

    public AuthUser getAuthUser() {
        return authUser;
    }

    public RefreshToken setAuthUser(AuthUser authUser) {
        this.authUser = authUser;
        return this;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public RefreshToken setRevoked(boolean revoked) {
        this.revoked = revoked;
        return this;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public RefreshToken setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public boolean isExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }
}
