package com.veeteq.auth.authservice.rest.api;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veeteq.auth.authservice.rest.dto.AuthResponseDto;
import com.veeteq.auth.authservice.rest.dto.LoginRequestDto;
import com.veeteq.auth.authservice.rest.dto.UserRegistrationDto;
import com.veeteq.auth.authservice.rest.dto.UserResponseDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("${app.api.base-path}/users")
public class AuthController implements AuthenticationApi {

    private final AuthenticationManager authManager;
    private final JwtEncoder jwtEncoder;

    // Token lifetime configurable via properties (default 3600s)
    @Value("${app.jwt.access-token-seconds:3600}")
    private long accessTokenSeconds;

    public AuthController(AuthenticationManager authManager, JwtEncoder jwtEncoder) {
        this.authManager = authManager;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/authenticate")
    @Override
	public ResponseEntity<AuthResponseDto> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequest) {
    	var authToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
    	var authentication = authManager.authenticate(authToken);

    	var authResponse = issueAccessToken(authentication);
    	
    	return ResponseEntity.ok(authResponse);
	}

	@Override
	public ResponseEntity<UserResponseDto> refreshToken(@Valid LoginRequestDto loginRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<Void> registerUser(@Valid UserRegistrationDto userRegistrationDto) {
		// TODO Auto-generated method stub
		return null;
	}

	private AuthResponseDto issueAccessToken(Authentication authentication) {
		Instant now = Instant.now();
    	Instant expires = now.plusSeconds(accessTokenSeconds);
    	
    	var scopes = authentication.getAuthorities().stream()
    	        .map(GrantedAuthority::getAuthority)
    	        .toList();

    	JwtClaimsSet claims = JwtClaimsSet.builder()
    	        .issuer("self")
    	        .issuedAt(now)
    	        .expiresAt(expires)
    	        .subject(authentication.getName())
    	        .claim("scope", scopes) // Przekazanie ról do tokena [4]    	        
    	        .build();

    	String token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    	
    	var authResponse = new AuthResponseDto()
    	.type("Bearer")
    	.token(token)
    	.expiresAt(expires.toString())
    	.roles(scopes);
    	
		return authResponse;
	}
}
