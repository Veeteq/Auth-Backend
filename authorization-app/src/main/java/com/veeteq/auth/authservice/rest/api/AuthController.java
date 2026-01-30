package com.veeteq.auth.authservice.rest.api;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veeteq.auth.authservice.rest.dto.AuthTokenResponseDto;
import com.veeteq.auth.authservice.rest.dto.LoginRequestDto;
import com.veeteq.auth.authservice.rest.dto.LoginResponseDto;
import com.veeteq.auth.authservice.rest.dto.UserRegistrationDto;
import com.veeteq.auth.authservice.service.AuthUserService;
import com.veeteq.auth.authservice.service.CookieService;
import com.veeteq.auth.authservice.service.RefreshTokenService;

@RestController
@RequestMapping("${app.api.base-path}/auth")
public class AuthController implements AuthenticationApi {

    private final AuthenticationManager authManager;
    private final JwtEncoder jwtEncoder;
    private final AuthUserService authUserService;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    // Token lifetime configurable via properties (default 3600s)
    @Value("${app.jwt.access-token-seconds:3600}")
    private long accessTokenSeconds;

    public AuthController(AuthenticationManager authManager, JwtEncoder jwtEncoder, AuthUserService authUserService, RefreshTokenService refreshTokenService, CookieService cookieService) {
        this.authManager = authManager;
        this.jwtEncoder = jwtEncoder;
        this.authUserService = authUserService;
        this.refreshTokenService = refreshTokenService;
        this.cookieService = cookieService;
    }

    /** LOGIN: issue access token + set refresh cookie */
    @Override
    @PostMapping(path = "/authenticate")
    public ResponseEntity<LoginResponseDto> loginUser(LoginRequestDto loginRequest) {
        var authToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
        var authentication = authManager.authenticate(authToken);

        var authUser = authUserService.findByUsername(authentication.getName()).orElseThrow();
        var refreshToken = refreshTokenService.issueToken(authUser);
        var cookie = cookieService.createCookie(refreshToken.getToken());
        var headers = new HttpHeaders();
        headers.add("Set-Cookie", cookie.toString());

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenSeconds);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("roles", roles)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        var response = new LoginResponseDto()
                .type("Bearer")
                .token(token)
                .expiresAt(LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC))
                .roles(roles)
                .user(null);

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    /** REFRESH: read refresh cookie, validate, rotate, return new access token */
    @Override
    @PostMapping(path = "/refresh")
    public ResponseEntity<AuthTokenResponseDto> refreshToken(String setCookie) {
        var cookieName = cookieService.getName();
        var cookieToken = extractCookie(setCookie, cookieName);

        if (cookieToken != null && !cookieToken.isBlank()) {
            //return ResponseEntity.status(401).body(Map.of("error", "Missing refresh token"));
        }

        var authUser = refreshTokenService.validateTokenAndGetAuthuser(cookieToken);

        // rotate: revoke old, issue new
        var rotated = refreshTokenService.rotateTokenForUser(authUser);

        // set the new cookie
        ResponseCookie cookie = cookieService.createCookie(rotated.getToken());
        var headers = new HttpHeaders();
        headers.add("Set-Cookie", cookie.toString());

        // issue new access token for the same principal
        var userRoles = authUser.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        var authentication = new UsernamePasswordAuthenticationToken(authUser.getUsername(), null, userRoles);

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenSeconds);

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("roles", roles)
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        var response = new AuthTokenResponseDto()
                .type("Bearer")
                .token(token)
                .expiresAt(LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC));

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    //@Override
    public ResponseEntity<Void> registerUser(UserRegistrationDto userRegistrationDto) {
        return null;
    }

    /** LOGOUT: revoke refresh token and clear cookie */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(String setCookie) {
        String cookieName = cookieService.getName();
        String cookieToken = extractCookie(setCookie, cookieName);
        if (cookieToken != null && !cookieToken.isBlank()) {
            refreshTokenService.revoke(cookieToken);
        }

        ResponseCookie cookie = cookieService.clearCookie();
        var headers = new HttpHeaders();
        headers.add("Set-Cookie", cookie.toString());

        return ResponseEntity.noContent()
                .headers(headers)
                .build();
    }

    private String extractCookie(String cookieHeader, String cookieName) {
        if (cookieHeader == null) return null;
        var cookieSearch = cookieName + "=";
        var result = Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(s -> s.startsWith(cookieSearch))
                .map(s -> s.substring(cookieSearch.length()))
                .findFirst()
                .orElse(null);
        return result;
    }

}
