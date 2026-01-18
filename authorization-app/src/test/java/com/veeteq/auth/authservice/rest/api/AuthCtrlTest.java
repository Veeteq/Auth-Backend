package com.veeteq.auth.authservice.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.entity.RefreshToken;
import com.veeteq.auth.authservice.rest.dto.LoginRequestDto;
import com.veeteq.auth.authservice.service.AuthUserService;
import com.veeteq.auth.authservice.service.CookieService;
import com.veeteq.auth.authservice.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters for slice test
public class AuthCtrlTest {

    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JwtEncoder jwtEncoder;
    @MockitoBean
    private AuthUserService userService;
    @MockitoBean
    private RefreshTokenService refreshTokenService;
    @MockitoBean
    private CookieService cookieService;

    @Value("${app.api.base-path}/auth")
    private String baseUrl;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp () {
    }

    @Test
    void authenticate_shouldReturnToken_andSetCookie() throws Exception {
        //given
        var dto = new LoginRequestDto()
                .username("jmclane")
                .password("123456");

        var authentication = new UsernamePasswordAuthenticationToken("demo", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        var user = new AuthUser().setId(1L).setUsername("demo");
        when(userService.findByUsername("demo")).thenReturn(Optional.of(user));

        when(refreshTokenService.issueToken(user)).thenReturn(new RefreshToken());

        when(cookieService.createCookie(any())).thenReturn(ResponseCookie.from("REFRESH_TOKEN", "x").build());

        var jwt = buildJwt();
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        mockMvc.perform(post(baseUrl.concat("/authenticate"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void authenticate_shouldFail_missingPassword() throws Exception {
        //given
        var dto = new LoginRequestDto()
                .username("jmclane");

        mockMvc.perform(post(baseUrl.concat("/authenticate"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("must not be null"));
    }

    private Jwt buildJwt() {
        var jwt = Jwt.withTokenValue("jwt-token")
                .header("alg", "RS256")
                .claim("roles", List.of("ROLE_USER"))
                .subject("demo")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        return jwt;
    }
}
