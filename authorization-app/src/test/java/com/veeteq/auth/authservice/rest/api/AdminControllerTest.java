package com.veeteq.auth.authservice.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.exception.UserAlreadyExistsException;
import com.veeteq.auth.authservice.exception.UserNotFoundException;
import com.veeteq.auth.authservice.mapper.AuthUserMapper;
import com.veeteq.auth.authservice.rest.dto.UserRegistrationDto;
import com.veeteq.auth.authservice.rest.dto.UserResponseDto;
import com.veeteq.auth.authservice.rest.dto.UserRoleDto;
import com.veeteq.auth.authservice.rest.dto.UserUpdateDto;
import com.veeteq.auth.authservice.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private AuthUserMapper authUserMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() throws Exception {

        var user = new AuthUser()
                .setId(1001L)
                .setUsername("jmclane");

        var dto = new UserResponseDto()
                .id(1001L)
                .username("jmclane")
                .email("john@acme.com")
                .firstName("John")
                .lastName("McLane")
                .enabled(true)
                .roles(List.of(UserRoleDto.ACCOUNT_ADMIN));

        when(adminService.getUsers()).thenReturn(List.of(user));
        when(authUserMapper.toDto(user)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1001))
                .andExpect(jsonPath("$[0].username").value("jmclane"));
    }

    @Test
    @DisplayName("Should return user by id")
    void shouldReturnUserById() throws Exception {

        var user = new AuthUser()
                .setId(1001L)
                .setUsername("jmclane");

        var dto = new UserResponseDto()
                .id(1001L)
                .username("jmclane")
                .email("john@acme.com")
                .firstName("John")
                .lastName("McLane")
                .enabled(true)
                .roles(List.of(UserRoleDto.ACCOUNT_ADMIN));

        when(adminService.findById(1001L))
                .thenReturn(user);

        when(authUserMapper.toDto(user))
                .thenReturn(dto);

        mockMvc.perform(get("/api/admin/users/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1001))
                .andExpect(jsonPath("$.username")
                        .value("jmclane"));
    }

    @Test
    @DisplayName("Should update user")
    void shouldUpdateUser() throws Exception {

        var request = new UserUpdateDto()
                .firstName("John")
                .lastName("McLane")
                .email("john.mclane@acme.com")
                .enabled(true);

        var updatedUser = new AuthUser()
                .setId(1001L)
                .setUsername("jmclane")
                .setEmail("john@acme.com")
                .setFirstname("John")
                .setLastname("McLane")
                .setEnabled(true);

        var response = new UserResponseDto()
                .id(1001L)
                .username("jmclane")
                .email("john@acme.com")
                .firstName("John")
                .lastName("McLane")
                .enabled(true)
                .roles(List.of(UserRoleDto.ACCOUNT_ADMIN));

        when(adminService.update(
                1001L,
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getEnabled()))
                .thenReturn(updatedUser);

        when(authUserMapper.toDto(updatedUser)).thenReturn(response);

        mockMvc.perform(put("/api/admin/users/1001")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@acme.com"));
    }

    @Test
    @DisplayName("Should delete user")
    void shouldDeleteUser() throws Exception {

        mockMvc.perform(delete("/api/admin/users/1001"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when user does not exist")
    void shouldReturnNotFoundForUnknownUser() throws Exception {

        when(adminService.findById(999L))
                .thenThrow(new UserNotFoundException("User not found: 999"));

        mockMvc.perform(get("/api/admin/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.detail")
                        .value("User not found: 999"));
    }

    @Test
    @DisplayName("Should reject duplicate username")
    void shouldRejectDuplicateUsername() throws Exception {

        var request = new UserRegistrationDto()
                .username("existingUser")
                .password("abcdefghijk")
                .email("john@example.com")
                .firstName("John")
                .lastName("McLane");

        when(adminService.update(eq(1001L), anyString(), anyString(), anyString(), anyBoolean()))
                .thenThrow(new UserAlreadyExistsException("Username already exists"));

        mockMvc.perform(put("/api/admin/users/1001")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.detail")
                        .value("Username already exists"));
    }

    @Test
    @DisplayName("Should reject duplicate email")
    void shouldRejectDuplicateEmail() throws Exception {

        var request = new UserRegistrationDto()
                .username("existingUser")
                .password("abcdefghijk")
                .email("john@example.com")
                .firstName("John")
                .lastName("McLane");

        when(adminService.update(eq(1001L), anyString(), anyString(), anyString(), anyBoolean()))
                .thenThrow(new UserAlreadyExistsException("Email already exists"));

        mockMvc.perform(put("/api/admin/users/1001")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFLICT"))
                .andExpect(jsonPath("$.detail")
                        .value("Email already exists"));
    }

}
