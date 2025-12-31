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
        @UniqueConstraint(name = "authusers_username_pk", columnNames = {"username"}),
        @UniqueConstraint(name = "authusers_email_pk", columnNames = {"email"})
})
public class AuthUser {

    @Id
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "firstname", nullable = false, length = 100)
    private String firstname;
    
    @Column(name = "lastname", nullable = false, length = 100)
    private String lastname;
    
    /** Roles, e.g., ROLE_USER, ROLE_ADMIN */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_roles_user"))
    )
    @Column(name = "role", nullable = false, length = 64)
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

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void addToRoles(String role) {
		this.roles.add(role);
	}

    
}
