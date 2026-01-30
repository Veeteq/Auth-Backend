package com.veeteq.auth.authservice.rest.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HealthCheckControllerTest {

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    @LocalManagementPort
    int managementPort;

    @Test
    public void testHealthEndpoint() throws Exception {
        ResponseEntity<JsonNode> response = restTemplateBuilder
                .rootUri("http://localhost:" + managementPort + "/actuator")
                .build().exchange("/health", HttpMethod.GET, new HttpEntity<>(null), JsonNode.class);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        var body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.at("/status").asText());
    }
}
