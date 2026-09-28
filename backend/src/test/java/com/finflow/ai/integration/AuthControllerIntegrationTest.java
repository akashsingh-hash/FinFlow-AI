package com.finflow.ai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.ai.module.auth.dto.LoginRequest;
import com.finflow.ai.module.auth.dto.RegisterRequest;
import com.finflow.ai.module.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Integration: Register -> Login -> Validate JWT tokens")
    void testRegisterAndLoginIntegration() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("integration_admin@testcorp.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("Admin")
                .companyName("Integration Test Corp")
                .taxId("US-TEST-999")
                .department("Executive")
                .build();

        // 1. Register
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("integration_admin@testcorp.com"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));

        assertThat(userRepository.existsByEmail("integration_admin@testcorp.com")).isTrue();

        // 2. Login
        LoginRequest loginReq = LoginRequest.builder()
                .email("integration_admin@testcorp.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("integration_admin@testcorp.com"));
    }

    @Test
    @DisplayName("Integration: Reject duplicate registration")
    void testDuplicateRegistration() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("admin@acme.com") // Seeded by DataInitializer
                .password("Password123!")
                .firstName("Dup")
                .lastName("User")
                .companyName("Acme Corp")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email address already in use."));
    }
}
