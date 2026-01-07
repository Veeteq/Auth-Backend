package com.veeteq.auth.authservice.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(value = CookieProperties.class)
public class RefreshCookieUtil {

	private String name;
	private String domain;
	private String path;
	private boolean secure;
	private boolean httpOnly;
    private final String sameSite;
    private final int maxAgeDays;

	public RefreshCookieUtil(CookieProperties cookieProperties, @Value("${app.jwt.refresh-token-days:14}") int maxAgeDays) {
		        this.name = cookieProperties.getName();
		        this.domain = cookieProperties.getDomain();
		        this.path = cookieProperties.getPath();
		        this.secure = cookieProperties.isSecure();
		        this.httpOnly = cookieProperties.isHttpOnly();
		        this.sameSite = cookieProperties.getSameSite();
		        this.maxAgeDays = maxAgeDays;
		        System.out.println(this.domain);
	}

    public ResponseCookie buildSetCookie(String token) {
        return ResponseCookie.from(name, token)
            .domain(domain)
            .path(path)
            .httpOnly(httpOnly)
            .secure(secure)
            .sameSite(sameSite)
            .maxAge(Duration.ofDays(maxAgeDays))
            .build();
    }

    public ResponseCookie buildClearCookie() {
        return ResponseCookie.from(name, "")
            .domain(domain)
            .path(path)
            .httpOnly(httpOnly)
            .secure(secure)
            .sameSite(sameSite)
            .maxAge(Duration.ZERO)
            .build();
    }

    public String getCookieName() { 
    	return name; 
    }

}
