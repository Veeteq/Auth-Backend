package com.veeteq.auth.authservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeteq.auth.authservice.rest.dto.LoginRequestDto;
import com.veeteq.auth.authservice.rest.dto.LoginResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoginJwtTokenTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Should generate JWT with document-mngr admin roles")
    void shouldGenerateJwtWithDocumentMngrAdminRoles() throws Exception {
        var request = new LoginRequestDto()
                .username("jmclane")
                .password("abc123456xyz");

        var mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        var response = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), LoginResponseDto.class);

        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getToken()).isNotBlank();

        assertThat(response.getRoles())
                .contains("USER_ROLE")
                .contains("ACCOUNT_ADMIN")
                .contains("DOCUMENT_ADMIN")
                .contains("ITEM_ADMIN");

        var jwt = jwtDecoder.decode(response.getToken());

        assertThat(jwt.getIssuer().toString()).isEqualTo("http://localhost:8282");

        assertThat(jwt.getSubject()).isEqualTo("jmclane");

        assertThat(jwt.getClaimAsStringList("roles"))
                .contains("USER_ROLE")
                .contains("ACCOUNT_ADMIN")
                .contains("DOCUMENT_ADMIN")
                .contains("ITEM_ADMIN");

        assertThat(jwt.getClaimAsString("iss").equals("http://localhost:8282"));
    }
}
