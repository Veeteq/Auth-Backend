package com.veeteq.auth.authservice.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "authusers", uniqueConstraints = {
        @UniqueConstraint(name = "authusers_username_uq", columnNames = {"username"}),
        @UniqueConstraint(name = "authusers_email_uq", columnNames = {"email"})
})
public class AuthUser {

    @Id
    @Column(name = "user_id")
    private Long id;

    /** Unique username for login */
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    /** BCrypt hashed password */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** Unique email for contact/login */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "firstname", nullable = false, length = 100)
    private String firstname;
    
    @Column(name = "lastname", nullable = false, length = 100)
    private String lastname;

    /** Enabled/disabled flag */
    @Column(nullable = false)
    private boolean enabled = true;

    /** Roles, e.g., ROLE_USER, ROLE_ADMIN */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id", foreignKey = @ForeignKey(name = "user_roles_user_fk")))
    @Column(name = "role_name", nullable = false, length = 64)
    private Set<String> roles = new HashSet<>();
    
    @Column(name = "create_time")
    @CreatedDate
    private Instant createdAt;

    @Column(name = "update_time")
    @LastModifiedDate
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public AuthUser setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AuthUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AuthUser setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public AuthUser setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getFirstname() {
        return firstname;
    }

    public AuthUser setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public AuthUser setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AuthUser setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public AuthUser addToRoles(String role) {
		this.roles.add(role);
        return this;
	}

}
