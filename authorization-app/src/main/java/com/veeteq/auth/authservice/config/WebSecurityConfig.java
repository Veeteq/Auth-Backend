package com.veeteq.auth.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.veeteq.auth.authservice.security.AuthUserDetailsService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    DaoAuthenticationProvider daoAuthProvider(AuthUserDetailsService uds, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Wyłączone dla statless API [4, 5]
            .cors(Customizer.withDefaults()) // Obsługa żądań z Angulara [8, 9]
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Brak sesji serwerowej [4, 5]
            .authorizeHttpRequests(auth -> auth
                // Publiczne endpointy zgodnie ze specyfikacją OpenAPI [10, 11]
                .requestMatchers(HttpMethod.POST, "/api/users/authenticate").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Dostęp do bazy in-memory
                // Pozostałe żądania wymagają uwierzytelnienia
                .anyRequest().authenticated()
            )
            // Konfiguracja serwera zasobów OAuth2/JWT przy użyciu Nimbus [5]
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // Obiekt odpowiedzialny za wyciąganie uprawnień z tokena
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        // Usuwamy domyślny przedrostek "SCOPE_", aby role 
        // pasowały bezpośrednio do tych w bazie (np. "ROLE_USER") [1]
        grantedAuthoritiesConverter.setAuthorityPrefix(""); 
        
        // Możesz też zmienić nazwę claima, jeśli w tokenie używasz "roles" zamiast "scope"
        // grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);        
        return jwtAuthenticationConverter;
    }
/*
    @Bean
    Converter<Jwt, JwtAuthenticationToken> jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName("roles"); // we will issue this claim in login
        gac.setAuthorityPrefix("");            // claims already like "ROLE_USER"

        return jwt -> {
            Collection<GrantedAuthority> authorities = gac.convert(jwt);
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // Usunięcie domyślnego prefiksu SCOPE_ [5]
        grantedAuthoritiesConverter.setAuthorityPrefix(""); 

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }    
/*
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder, Converter<Jwt, JwtAuthenticationToken> jwtAuthConverter) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // dev only
                .anyRequest().authenticated())
            .headers(h -> h.frameOptions(f -> f.sameOrigin())) // allow H2 console frames
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthConverter)));

        return http.build();
    }
*/
}
