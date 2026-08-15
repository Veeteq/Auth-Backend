package com.veeteq.auth.authservice.rest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeteq.auth.authservice.config.JacksonProblemConfig;
import com.veeteq.auth.authservice.entity.AuthUser;
import com.veeteq.auth.authservice.entity.RefreshToken;
import com.veeteq.auth.authservice.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.veeteq.auth.authservice.rest.dto.LoginRequestDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // disable security filters for slice test
@Import(JacksonProblemConfig.class)
public class AuthController3Test {
    private static final String USERNAME = "jmclane";
    private static final String PASSWORD = "abc123456xyz";
    private static final String AUTHENTICATED_USERNAME = "demo";
    private static final String ACCESS_TOKEN = "jwt-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "REFRESH_TOKEN";
    private static final String ISSUER = "http://localhost:8282";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthUserService authUserService;

    @MockitoBean
    private AccessTokenService accessTokenService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private CookieService cookieService;

    @Value("${app.api.base-path}/auth")
    private String baseUrl;


    @Test
    @DisplayName("Should authenticate user, return access token and set refresh token cookie")
    void authenticate_shouldReturnToken_andSetCookie() throws Exception {
        //given
        var request = createLoginRequest();
        var authentication = new UsernamePasswordAuthenticationToken(AUTHENTICATED_USERNAME, null,List.of(new SimpleGrantedAuthority("ROLE_USER")));
        var authUser = new AuthUser()
                .setId(1L)
                .setUsername(AUTHENTICATED_USERNAME);

        var refreshToken = new RefreshToken()
                .setToken(REFRESH_TOKEN);

        var refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN)
                .httpOnly(true)
                .path("/")
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authUserService.findByUsername(AUTHENTICATED_USERNAME)).thenReturn(Optional.of(authUser));
        var accessTokenResult = new AccessTokenResult(ACCESS_TOKEN, Instant.parse("2026-08-15T18:00:00Z"), List.of("USER_ROLE", "ACCOUNT_ADMIN", "DOCUMENT_ADMIN", "ITEM_ADMIN"));
        when(accessTokenService.issueToken(any(Authentication.class))).thenReturn(accessTokenResult);
        when(refreshTokenService.issueToken(authUser)).thenReturn(refreshToken);
        when(cookieService.createCookie(REFRESH_TOKEN)).thenReturn(refreshCookie);
        when(jwtEncoder.encode(any())).thenReturn(createJwt());

        var jwt = buildJwt();
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        mockMvc.perform(post(baseUrl.concat("/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.token").value(ACCESS_TOKEN))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles").value(containsInAnyOrder("USER_ROLE", "ACCOUNT_ADMIN", "DOCUMENT_ADMIN", "ITEM_ADMIN")));
    }

    @Test
    @DisplayName("Should reject login request when password is missing")
    void authenticate_shouldFail_missingPassword() throws Exception {
        var request = new LoginRequestDto()
                .username(USERNAME);

        mockMvc.perform(post(baseUrl.concat("/login"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[0].field").value("password"))
                .andExpect(jsonPath("$.violations[0].message").value("must not be null"));
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

    private LoginRequestDto createLoginRequest() {
        return new LoginRequestDto()
                .username(USERNAME)
                .password(PASSWORD);
    }

    private Jwt createJwt() {
        var now = Instant.now();

        return Jwt.withTokenValue(ACCESS_TOKEN)
                .header("alg", "RS256")
                .issuer(ISSUER)
                .subject(AUTHENTICATED_USERNAME)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("roles", List.of("ROLE_USER"))
                .build();
    }

}
