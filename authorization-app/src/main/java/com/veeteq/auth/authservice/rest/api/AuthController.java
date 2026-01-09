package com.veeteq.auth.authservice.rest.api;

import com.veeteq.auth.authservice.rest.dto.AuthResponseDto;
import com.veeteq.auth.authservice.rest.dto.LoginRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/auth")
public class AuthController { //implements AuthenticationApi {

    private final AuthenticationManager authManager;
    private final JwtEncoder jwtEncoder;

    // Token lifetime configurable via properties (default 3600s)
    @Value("${app.jwt.access-token-seconds:3600}")
    private long accessTokenSeconds;

    public AuthController(AuthenticationManager authManager, JwtEncoder jwtEncoder) {
        this.authManager = authManager;
        this.jwtEncoder = jwtEncoder;
    }

    //@Override
    @PostMapping(path = "/authenticate")
    public ResponseEntity<AuthResponseDto> authenticateUser(@RequestBody LoginRequestDto loginRequest) {
        var authToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        var authentication = authManager.authenticate(authToken);

        Instant now = Instant.now();
        Instant expires = now.plusSeconds(accessTokenSeconds);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(expires)
                .claim("roles", roles)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        var response = new AuthResponseDto()
                .type("Bearer")
                .token(token)
                .expiresAt(expires.toString())
                .roles(roles)
                .user(null);

        return ResponseEntity.ok(response);
    }
}
