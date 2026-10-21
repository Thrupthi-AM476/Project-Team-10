package com.vps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // (1) Successful registration returns 201 with token and role=CUSTOMER
    @Test
    void register_success_returns201WithTokenAndCustomerRole() throws Exception {
        Map<String, String> body = Map.of(
                "fullName", "Bob Jones",
                "email", "bob@example.com",
                "password", "Password1!"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    // (2) Duplicate email registration returns 400
    @Test
    void register_duplicateEmail_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "fullName", "Carol White",
                "email", "carol@example.com",
                "password", "Password1!"
        );

        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // Second registration with the same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // (3) Role field in JSON is ignored — always returns CUSTOMER
    @Test
    void register_withRoleFieldInJson_alwaysReturnsCustomer() throws Exception {
        // Sending "role": "ADMINISTRATOR" in the JSON body; the field is ignored because
        // RegisterRequest no longer has a role field (unknown fields are silently ignored by Jackson)
        Map<String, Object> body = Map.of(
                "fullName", "Dave Admin",
                "email", "dave@example.com",
                "password", "Password1!",
                "role", "ADMINISTRATOR"
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }

    // (4) Weak password (no special character) returns 400
    @Test
    void register_weakPassword_returns400() throws Exception {
        Map<String, String> body = Map.of(
                "fullName", "Eve Simple",
                "email", "eve@example.com",
                "password", "Password1"  // missing special character
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // (5) Successful login returns 200 with token
    @Test
    void login_success_returns200WithToken() throws Exception {
        // Register first
        Map<String, String> registerBody = Map.of(
                "fullName", "Frank Login",
                "email", "frank@example.com",
                "password", "Password1!"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated());

        // Then login
        Map<String, String> loginBody = Map.of(
                "email", "frank@example.com",
                "password", "Password1!"
        );
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    // (6) Wrong password returns 400
    @Test
    void login_wrongPassword_returns400() throws Exception {
        Map<String, String> registerBody = Map.of(
                "fullName", "Grace Wrong",
                "email", "grace@example.com",
                "password", "Password1!"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated());

        Map<String, String> loginBody = Map.of(
                "email", "grace@example.com",
                "password", "WrongPass1!"
        );
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isBadRequest());
    }

    // (7) Locked account login returns 403
    @Test
    void login_lockedAccount_returns403() throws Exception {
        // Register the account
        Map<String, String> registerBody = Map.of(
                "fullName", "Henry Locked",
                "email", "henry@example.com",
                "password", "Password1!"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated());

        // Trigger 5 failed login attempts to lock the account
        Map<String, String> badLogin = Map.of(
                "email", "henry@example.com",
                "password", "WrongPass1!"
        );
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badLogin)))
                    .andExpect(status().isBadRequest());
        }

        // Now even correct credentials are blocked (account is locked)
        Map<String, String> correctLogin = Map.of(
                "email", "henry@example.com",
                "password", "Password1!"
        );
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctLogin)))
                .andExpect(status().isForbidden());
    }
}
