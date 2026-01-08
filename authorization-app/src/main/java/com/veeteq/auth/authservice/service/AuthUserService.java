package com.veeteq.auth.authservice.service;

import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.repository.AuthUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public AuthUser register(String username, String password, String email, String firstname, String lastname) {
        var user = new AuthUser()
                .setId(1L)
                .setUsername(username)
                .setPassword(passwordEncoder.encode(password))
                .setEmail(email)
                .setFirstname(firstname)
                .setLastname(lastname)
                .setEnabled(true)
                .addToRoles("ROLE_USER");

        var savedUser = authUserRepository.save(user);
        return savedUser;
    }
}
