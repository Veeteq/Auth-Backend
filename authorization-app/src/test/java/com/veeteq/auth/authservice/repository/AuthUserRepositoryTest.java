package com.veeteq.auth.authservice.repository;

import com.veeteq.auth.authservice.entity.AuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AuthUserRepositoryTest {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Test
    @Rollback(value = false)
    void testSaveAndFindByUsername() {
        var user = new AuthUser()
                .setId(11L)
                .setUsername("testuser_1")
                .setPassword("password")
                .setEmail("testuser.1@example.com")
                .setFirstname("Test 1")
                .setLastname("User")
                .setEnabled(true);
        authUserRepository.save(user);

        Optional<AuthUser> foundUser = authUserRepository.findByUsername("testuser_1");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser_1");
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser.1@example.com");
    }

    @Test
    @Rollback(value = false)
    void testSaveAndFindByEmail() {
        var user = new AuthUser()
                .setId(12L)
                .setUsername("testuser_2")
                .setPassword("password")
                .setEmail("testuser.2@example.com")
                .setFirstname("Test 2")
                .setLastname("User")
                .setEnabled(true);
        authUserRepository.save(user);

        Optional<AuthUser> foundUser = authUserRepository.findByEmail("testuser.2@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser_2");
        assertThat(foundUser.get().getEmail()).isEqualTo("testuser.2@example.com");
    }
}
