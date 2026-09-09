package com.veeteq.auth.authservice.service;

import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.entity.UserRole;
import com.veeteq.auth.authservice.exception.UserNotFoundException;
import com.veeteq.auth.authservice.repository.AuthUserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AdminService {

    private final AuthUserRepository authUserRepository;

    public AdminService(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Transactional(readOnly = true)
    public List<AuthUser> getUsers() {
        return authUserRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AuthUser findById(Long id) {
        return authUserRepository
                .findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public AuthUser update(Long id, String email, String firstname, String lastname, boolean enabled) {
        var user = findById(id);
        user.setEmail(email);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setEnabled(enabled);
        return authUserRepository.save(user);
    }
    @Transactional
    public void deleteById(Long id) {
        AuthUser user = findById(id);
        authUserRepository.delete(user);
    }

    @Transactional
    public AuthUser enable(Long id) {
        var user = findById(id);
        user.setEnabled(true);
        return authUserRepository.save(user);
    }

    @Transactional
    public AuthUser disable(Long id) {
        var user = findById(id);
        user.setEnabled(false);
        return authUserRepository.save(user);
    }

    @Transactional
    public AuthUser updateRoles(Long id, Set<UserRole> roles) {
        if (roles == null || roles.isEmpty()) throw new IllegalArgumentException("At least one role must be assigned");

        var user = findById(id);

        String currentUsername = getCurrentUsername();
        boolean editingOwnAccount = currentUsername.equals(user.getUsername());
        if (editingOwnAccount && !roles.contains(UserRole.ACCOUNT_ADMIN)) {
            throw new IllegalArgumentException("You cannot remove ACCOUNT_ADMIN from your own account");
        }

        user.getRoles().clear();
        user.getRoles().addAll(roles);
        return authUserRepository.save(user);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext() .getAuthentication();
        if (authentication == null) throw new IllegalStateException("No authenticated user");
        return authentication.getName();
    }
}
