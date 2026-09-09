package com.veeteq.auth.authservice.service;

import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.entity.UserRole;
import com.veeteq.auth.authservice.exception.UserNotFoundException;
import com.veeteq.auth.authservice.repository.AuthUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @Mock
    private AuthUserRepository repository;

    @InjectMocks
    private AdminService service;

    @Test
    void shouldFindUser() {

        AuthUser user = new AuthUser();
        user.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        var result = service.findById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldUpdateUser() {

        var user = new AuthUser()
                .setId(1L)
                .setUsername("john");

        when(repository.findById(1L)).thenReturn(Optional.of(user));
        when(repository.save(any(AuthUser.class))).thenAnswer(i -> i.getArgument(0));

        var result = service.update(
                1L,
                "john@example.com",
                "John",
                "McLane",
                true);

        assertEquals("john@example.com", result.getEmail());
        assertEquals("John", result.getFirstname());

        verify(repository).save(any(AuthUser.class));
    }

    @Test
    void shouldRejectEmptyRoleSet() {
        assertThrows(IllegalArgumentException.class, () -> service.updateRoles(1L, Set.of()));
    }

    @Test
    void shouldPreventRemovingOwnAdminRole() {
        var user = new AuthUser()
                .setId(1L)
                .setUsername("jmclane")
                .addToRoles(UserRole.ACCOUNT_ADMIN);

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        var authentication = new UsernamePasswordAuthenticationToken("jmclane", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(IllegalArgumentException.class, () -> service.updateRoles(1L,Set.of(UserRole.USER_ROLE)));
    }

    @Test
    void shouldAllowEditingAnotherUsersRoles() {
        var user = new AuthUser()
                .setId(2L)
                .setUsername("john")
                .addToRoles(UserRole.ACCOUNT_ADMIN);

        when(repository.findById(2L)).thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", null));

        var result = service.updateRoles(2L, Set.of(UserRole.DOCUMENT_ADMIN));

        assertTrue(result.getRoles().contains(UserRole.DOCUMENT_ADMIN));
    }

    @Test
    void shouldThrowUserNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.findById(999L));
    }

}
/**
 * Recommended minimum test coverage
 *
 * For the work item "Implement DTOs + Mapper + AdminController" I'd create:
 *
 * Mapper
 * toDto()
 * toEntity()
 *
 * AdminController
 * ADMIN can access endpoint
 * USER cannot access endpoint
 * anonymous cannot access endpoint
 * happy path response
 *
 * Service
 * successful case
 * entity not found
 * validation failure (if applicable)
 */