package com.veeteq.auth.authservice.rest.api;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import com.veeteq.auth.authservice.config.RefreshCookieUtil;
import com.veeteq.auth.authservice.entity.RefreshToken;
import com.veeteq.auth.authservice.rest.dto.LoginRequestDto;
import com.veeteq.auth.authservice.rest.dto.UserRegistrationDto;
import com.veeteq.auth.authservice.rest.dto.UserResponseDto;
import com.veeteq.auth.authservice.security.AuthUserDetailsService;
import com.veeteq.auth.authservice.security.RefreshTokenService;
import com.veeteq.auth.authservice.service.AuthUserService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${app.api.base-path}/auth")
public class Bla {

    private final AuthenticationManager authManager;
    private final JwtEncoder jwtEncoder;
    private final AuthUserService userService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshCookieUtil cookieUtil;

    @Value("${app.jwt.access-token-seconds:3600}")
    private long accessTokenSeconds;

    public Bla(AuthenticationManager authManager, JwtEncoder jwtEncoder, AuthUserService userService, RefreshTokenService refreshTokenService, RefreshCookieUtil cookieUtil) {
        this.authManager = authManager;
        this.jwtEncoder = jwtEncoder;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.cookieUtil = cookieUtil;
    }

    /** LOGIN: issue access token + set refresh cookie */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDto req, HttpServletResponse res) {
    	var authToken = new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());
    	var authentication = authManager.authenticate(authToken);

        String accessToken = issueAccessToken(authentication);
        // Issue refresh token cookie
        var user = userService.findByUsername(authentication.getName())
            .orElseThrow(); // should exist since we authenticated

        RefreshToken rt = refreshTokenService.issue(user);
        ResponseCookie cookie = cookieUtil.buildSetCookie(rt.getToken());
        res.addHeader("Set-Cookie", cookie.toString());

        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        return ResponseEntity.ok(Map.of(
            "token", accessToken,
            "expiresIn", accessTokenSeconds,
            "roles", roles
        ));
    }
    
    /** REGISTER */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto req) {
        var user = userService.register(req.getUsername(), req.getFirstName(), req.getLastName(), req.getEmail(), req.getPassword());
        var response = new UserResponseDto()
        		.id(user.getId().intValue())
        		.firstName(user.getFirstname())
        		.lastName(user.getLastname())
        		.email(user.getEmail())
        		.username(user.getUsername())
        		.roles(user.getRoles().stream().toList());
        return ResponseEntity.ok(response);
    }



    /** REFRESH: read refresh cookie, validate, rotate, return new access token */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest req, HttpServletResponse res) {
        String cookieHeader = req.getHeader("Cookie");
        String cookieName = cookieUtil.getCookieName();
        String refreshValue = extractCookie(cookieHeader, cookieName);
        if (refreshValue == null || refreshValue.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing refresh token"));
        }

        RefreshToken valid = refreshTokenService.validate(refreshValue);
        // rotate: revoke old, issue new
        RefreshToken rotated = refreshTokenService.rotate(valid);
        // set the new cookie
        ResponseCookie cookie = cookieUtil.buildSetCookie(rotated.getToken());
        res.addHeader("Set-Cookie", cookie.toString());

        // issue new access token for the same principal
        var user = valid.getAuthUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user.getUsername(), null,
            user.getRoles().stream().map(org.springframework.security.core.authority.SimpleGrantedAuthority::new).toList()
        );

        String newAccess = issueAccessToken(authentication);

        return ResponseEntity.ok(Map.of(
            "token", newAccess,
            "expiresIn", accessTokenSeconds
        ));
    }

    /** LOGOUT: revoke refresh token and clear cookie */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest req, HttpServletResponse res) {
        String cookieHeader = req.getHeader("Cookie");
        String cookieName = cookieUtil.getCookieName();
        String refreshValue = extractCookie(cookieHeader, cookieName);
        if (refreshValue != null && !refreshValue.isBlank()) {
            refreshTokenService.revoke(refreshValue);
        }
        ResponseCookie cleared = cookieUtil.buildClearCookie();
        res.addHeader("Set-Cookie", cleared.toString());
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    private String issueAccessToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(accessTokenSeconds);
        
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(authentication.getName())
            .issuedAt(now)
            .expiresAt(expires)
            .claim("roles", roles) // matches SecurityConfig's converter
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /** Extract cookie value by name from "Cookie" header (dev-friendly). */
    private String extractCookie(String cookieHeader, String name) {
        if (cookieHeader == null) return null;
        return Arrays.stream(cookieHeader.split(";"))
            .map(String::trim)
            .filter(c -> c.startsWith(name + "="))
            .map(c -> c.substring((name + "=").length()))
            .findFirst()
            .orElse(null);
    }
}

