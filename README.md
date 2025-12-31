Chunk 1 — Project & Dependencies Setup
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