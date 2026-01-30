package com.veeteq.auth.authservice.config;

import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationTokenConverter() {
        var gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthorityPrefix("");
        gac.setAuthoritiesClaimName("roles");

        return jwt -> {
            Collection<GrantedAuthority> authorities = gac.convert(jwt);
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder, Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationTokenConverter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll() // Allow access to health endpoint
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // dev only
                        .anyRequest().authenticated())
                .headers(headers -> headers.frameOptions(fo -> fo.sameOrigin()))
                //.exceptionHandling(e -> e.authenticationEntryPoint(null))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationTokenConverter)));
        return http.build();
    }
}
