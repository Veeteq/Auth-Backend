package com.veeteq.auth.authservice.service;

import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.entity.UserRole;
import com.veeteq.auth.authservice.exception.UserAlreadyExistsException;
import com.veeteq.auth.authservice.repository.AuthUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthUserService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthUserService(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Optional<AuthUser> findByUsername(String username) {
        return authUserRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<AuthUser> findByEmail(String email) {
        return authUserRepository.findByEmail(email);
    }

    @Transactional
    public AuthUser register(String username, String password, String email, String firstname, String lastname) {
        if (authUserRepository.existsByUsername(username)) throw new UserAlreadyExistsException("Username already exists");

        if (authUserRepository.existsByEmail(email)) throw new UserAlreadyExistsException("Email already exists");

        var user = new AuthUser()
                .setUsername(username)
                .setPassword(passwordEncoder.encode(password))
                .setEmail(email)
                .setFirstname(firstname)
                .setLastname(lastname)
                .setEnabled(true)
                .addToRoles(UserRole.USER_ROLE);

        var savedUser = authUserRepository.save(user);
        return savedUser;
    }

}
