package com.veeteq.auth.authservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration cfg = new CorsConfiguration();
        
        cfg.setAllowedOrigins(properties.getAllowedOrigins());
        cfg.setAllowedMethods(properties.getAllowedMethods());
        cfg.setAllowedHeaders(properties.getAllowedHeaders());
        cfg.setAllowCredentials(Boolean.TRUE.equals(properties.getAllowCredentials()));
        // If you need to expose headers to the browser (e.g., Authorization), add:
        // cfg.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

}
