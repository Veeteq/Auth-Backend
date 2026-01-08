package com.veeteq.auth.authservice.rest.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
public class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHelloEndpointUnauthorized() throws Exception {
        // Perform a GET request to /api/hello without authentication
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isUnauthorized()); // Expect HTTP 401 Unauthorized
    }

    @WithMockUser
    @Test
    public void testHelloEndpointAuthorized() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk()); // Expect HTTP 200 OK
    }

}
