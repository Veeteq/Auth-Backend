package com.veeteq.auth.authservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.repository.AuthUserRepository;

import jakarta.validation.constraints.NotNull;

@Service
public class AuthUserService {
	private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    
	public AuthUserService(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
		this.authUserRepository = authUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

    public Optional<AuthUser> findByUsername(String username) {
        return authUserRepository.findByUsername(username);
    }

    public Optional<AuthUser> findByEmail(String email) {
        return authUserRepository.findByEmail(email);
    }

    public List<AuthUser> findAll() {
        return authUserRepository.findAll();
    }

	public AuthUser register(@NotNull String username, @NotNull String firstName, @NotNull String lastName, String email, @NotNull String password) {
		if (authUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (authUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        AuthUser user = new AuthUser();
        user.setUsername(username);
        user.setFirstname(firstName);
        user.setLastname(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        //user.setDisplayName(displayName);
        //user.setEnabled(true);
        user.addToRoles("ROLE_USER");
        return authUserRepository.save(user);

	}


}
