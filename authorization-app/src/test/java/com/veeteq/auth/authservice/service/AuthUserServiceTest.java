package com.veeteq.auth.authservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.repository.AuthUserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthUserServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthUserService authUserService;

    private AuthUser user;

    @BeforeEach
    void setUp() {
        user = new AuthUser()
                .setId(1L)
                .setUsername("testuser")
                .setPassword("password")
                .setEmail("test@example.com")
                .setFirstname("Test")
                .setLastname("User")
                .setEnabled(true);
    }

    @Test
    void testFindUserByUsername() {
        when(authUserRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<AuthUser> foundUser = authUserService.findByUsername("testuser");

        assertTrue(foundUser.isPresent());

        var authUser = foundUser.get();
        assertEquals("testuser", authUser.getUsername());
        verify(authUserRepository).findByUsername("testuser");
    }

    @Test
    void testFindUserByEmail() {
        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<AuthUser> foundUser = authUserService.findByEmail("test@example.com");

        assertTrue(foundUser.isPresent());

        var authUser = foundUser.get();
        assertEquals("test@example.com", authUser.getEmail());
        verify(authUserRepository).findByEmail("test@example.com");
    }

    @Test
    public void testRegister() {
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");

        AuthUser registeredUser = authUserService.register("testuser", "password", "test@example.com", "Test", "User");

        // Capture the argument passed to the save method
        ArgumentCaptor<AuthUser> userCaptor = forClass(AuthUser.class);
        verify(authUserRepository).save(userCaptor.capture());

        // Verify the properties of the captured user
        AuthUser savedUser = userCaptor.getValue();

        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("hashedPassword", savedUser.getPassword());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("Test", savedUser.getFirstname());
        assertEquals("User", savedUser.getLastname());
        assertTrue(savedUser.isEnabled());
        assertTrue(savedUser.getRoles().contains("ROLE_USER"));
    }
}
