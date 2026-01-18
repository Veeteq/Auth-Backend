package com.veeteq.auth.authservice.service;

import com.veeteq.auth.authservice.config.CookieProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@EnableConfigurationProperties(CookieProperties.class)
public class CookieService {

    private String name;
    private String domain;
    private String path;
    private boolean httpOnly;
    private boolean secure;
    private String sameSite;
    private long maxAge;

    CookieService(CookieProperties cookieProperties, @Value("${app.jwt.refresh-token-days:14}") int maxAgeDays) {
        this.name = cookieProperties.getName();
        this.domain = cookieProperties.getDomain();
        this.path = cookieProperties.getPath();
        this.httpOnly = cookieProperties.isHttpOnly();
        this.secure = cookieProperties.isSecure();
        this.sameSite = cookieProperties.getSameSite();
        this.maxAge = maxAgeDays;
    }

    public ResponseCookie createCookie(String token) {
        var cookie = ResponseCookie.from(name, token)
                .domain(domain)
                .path(path)
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(Duration.ofDays(maxAge))
                .build();
        return cookie;
    }

    public ResponseCookie clearCookie() {
        var cookie = ResponseCookie.from(name, "")
                .domain(domain)
                .path(path)
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite(sameSite)
                .maxAge(Duration.ZERO)
                .build();
        return cookie;
    }

    public String getName() {
        return this.name;
    }

}
