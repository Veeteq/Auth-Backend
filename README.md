# Chunk 1 — Project & Dependencies Setup
Goal: Create a Spring Boot project wired for web, security, data persistence, and JWT.
What you’ll do:

Create a new Spring Boot project (Maven or Gradle).
Add dependencies:

spring-boot-starter-web
spring-boot-starter-security
spring-boot-starter-data-jpa
Database driver (e.g., postgresql or h2 for dev)
JWT: either jjwt-api, jjwt-impl, jjwt-jackson or use Spring’s spring-security-oauth2-jose (Nimbus JOSE).


Set basic application.yml (server port, datasource, JPA settings, CORS placeholder, API prefix).
Decide JWT signing strategy: HS256 (shared secret) vs RS256 (key pair). For production, prefer RS256.

Validation:

App starts: ./mvnw spring-boot:run or ./gradlew bootRun.
/actuator/health (if actuator added) returns UP, or base / returns 404 (normal).

# Chunk 2 — Domain Model & Persistence
Goal: Define user accounts, roles/authorities, and persistence layer.
What you’ll do:

Create entities:

User (id, username/email, password (BCrypt), enabled, createdAt).
Role (id, name) or keep roles as a collection of strings for simplicity.


Create JPA repositories:

UserRepository with findByUsername/findByEmail.


Seed a dev user (data.sql or CommandLineRunner).
DTOs: LoginRequest, RegisterRequest, UserResponse.

Validation:

Run app and verify the dev user exists (e.g., via repository test or temporary endpoint).

# Chunk 3 — Security Configuration & JWT Plumbing
Goal: Configure Spring Security and JWT verification on protected endpoints.
What you’ll do:

Define a SecurityConfig:

Permit /auth/** (login/register/refresh)
Protect /api/**
Configure CORS allowed origin for Angular (http://localhost:4200 by default).
Password encoder: BCryptPasswordEncoder.


Implement UserDetailsService to load users by username.
Implement JWT utilities:

Create/verify token
Claims: sub, roles, exp, iat


Implement a JwtAuthenticationFilter:

Reads Authorization: Bearer <token>
Validates token, sets SecurityContext.


Validation:

Protected endpoint /api/ping returns 401 without token.
With a valid token in Authorization header, returns 200.

- SecurityFilterChain with /auth/** permitted and /api/** protected
- CorsConfigurationSource wired to your application.yml
- JwtEncoder/JwtDecoder beans (RS256 keypair for dev)
- A POST /auth/login endpoint example returning { token }