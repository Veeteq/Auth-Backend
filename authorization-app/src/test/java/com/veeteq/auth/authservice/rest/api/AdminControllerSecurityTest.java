package com.veeteq.auth.authservice.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeteq.auth.authservice.config.WebSecurityConfig;
import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.entity.UserRole;
import com.veeteq.auth.authservice.mapper.AuthUserMapper;
import com.veeteq.auth.authservice.rest.dto.UserResponseDto;
import com.veeteq.auth.authservice.rest.dto.UserRoleDto;
import com.veeteq.auth.authservice.rest.dto.UserRolesDto;
import com.veeteq.auth.authservice.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(WebSecurityConfig.class)
@TestPropertySource(properties = { "app.api.base-path=/api" })
public class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private AuthUserMapper authUserMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    //@MockitoBean
    //private Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationTokenConverter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should allow ACCOUNT_ADMIN to access users endpoint")
    @WithMockUser(authorities = "ACCOUNT_ADMIN")
    void shouldReturnUsersForAdmin() throws Exception {

        when(adminService.getUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject USER to access users endpoint")
    @WithMockUser(authorities = "USER_ROLE")
    void shouldRejectRegularUser() throws Exception {

        mockMvc.perform(get("/api/admin/users"))
                .andDo(print())
                .andExpect(status().isForbidden());
        verifyNoInteractions(adminService);
    }

    @Test
    @DisplayName("Should reject anonymous user to access users endpoint")
    void shouldRejectAnonymous() throws Exception {

        mockMvc.perform(get("/api/admin/users"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(adminService);
    }

    @Test
    @WithMockUser(authorities = "ACCOUNT_ADMIN")
    void shouldAllowDeleteForAccountAdmin() throws Exception {

        mockMvc.perform(delete("/api/admin/users/1001"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "ACCOUNT_ADMIN")
    void shouldUpdateRoles() throws Exception {

        var request = new UserRolesDto();
        request.setRoles(Set.of(UserRoleDto.ACCOUNT_ADMIN, UserRoleDto.DOCUMENT_ADMIN));

        var updatedUser = new AuthUser()
                .setId(1001L)
                .setUsername("jmclane")
                .addToRoles(UserRole.ACCOUNT_ADMIN)
                .addToRoles(UserRole.DOCUMENT_ADMIN);

        when(adminService.updateRoles(eq(1001L), anySet()))
                .thenReturn(updatedUser);

        when(authUserMapper.toDto(updatedUser))
                .thenReturn(new UserResponseDto()
                                .id(1001L)
                                .username("jmclane")
                                .roles(List.of(UserRoleDto.ACCOUNT_ADMIN, UserRoleDto.DOCUMENT_ADMIN)));

        var json = objectMapper.writeValueAsString(request);
        mockMvc.perform(put("/api/admin/users/1001/roles")
                                .contentType(APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "USER_ROLE")
    void shouldRejectRoleUpdateForRegularUser() throws Exception {

        mockMvc.perform(put("/api/admin/users/1001/roles")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "roles" : [ "DOCUMENT_ADMIN", "ACCOUNT_ADMIN" ]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER_ROLE")
    void shouldRejectUserEnablementForRegularUser() throws Exception {

        mockMvc.perform(patch("/api/admin/users/1001/enable"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}
