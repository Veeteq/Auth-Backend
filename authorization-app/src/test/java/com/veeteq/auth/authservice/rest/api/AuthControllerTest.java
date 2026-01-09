package com.veeteq.auth.authservice.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import com.veeteq.auth.authservice.rest.dto.AuthResponseDto;
import com.veeteq.auth.authservice.rest.dto.LoginRequestDto;

public class AuthControllerTest {
	
    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAuthenticateUser_Success() {
        var loginRequest = new LoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");
        
        var authentication = new TestingAuthenticationToken("testuser", "password", "ROLE_USER");
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        var now = Instant.now();
        var expiresAt = Instant.now().plusSeconds(3600);
        
        String tokenValue = "mockJwtToken";
        var claims = JwtClaimsSet.builder()
                .subject("testuser")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("roles", List.of("ROLE_USER"))
                .build();
        Jwt jwt = Jwt.withTokenValue(tokenValue)
        	      .header("alg", "none")
        	      .claims(c -> c.putAll(claims.getClaims()))
        	      .build();
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        // Act
        var response = authController.authenticateUser(loginRequest);

        // Assert
        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
        AuthResponseDto responseBody = response.getBody();
        assert responseBody != null; // Ensure response body is not null
        assertEquals("Bearer", responseBody.getType());
        assertEquals(tokenValue, responseBody.getToken());
        //assertEquals(expiresAt.toString(), responseBody.getExpiresAt());
        assertEquals(List.of("ROLE_USER"), responseBody.getRoles());
    }
    
    @Test
    void testAuthenticateUser_Failure() {
        var loginRequest = new LoginRequestDto();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new RuntimeException("Authentication failed"));

        var exc = assertThrows(RuntimeException.class, () -> authController.authenticateUser(loginRequest));
        assertNotNull(exc);
        assertEquals("Authentication failed", exc.getMessage());
    }
}
