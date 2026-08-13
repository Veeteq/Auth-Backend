package com.veeteq.auth.authservice.rest.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.servlet.LogbookFilter;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class AuthController2Test {

    @Autowired
    private WebApplicationContext wac;

    private  MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilters(new LogbookFilter(Logbook.create()))
                .build();
    }
    @Test
    void test_UserNotFound_returns401Unauthorized() throws Exception {

        var request = """
      {"username":"ghost.user","password":"doesNotMatter123"}
    """;

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE)) // no refresh cookie on 401
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.status").value(401));
    }
}
