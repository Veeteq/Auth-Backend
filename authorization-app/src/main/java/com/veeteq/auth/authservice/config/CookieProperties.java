package com.veeteq.auth.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cookie.refresh")
public class CookieProperties {

    private String name;
    private String domain;
    private String path;
    private boolean secure;
    private boolean httpOnly;
    private String sameSite;

    public String getName() {
        return name;
    }

    public CookieProperties setName(String name) {
        this.name = name;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public CookieProperties setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public String getPath() {
        return path;
    }

    public CookieProperties setPath(String path) {
        this.path = path;
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public CookieProperties setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public CookieProperties setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    public String getSameSite() {
        return sameSite;
    }

    public CookieProperties setSameSite(String sameSite) {
        this.sameSite = sameSite;
        return this;
    }
}
