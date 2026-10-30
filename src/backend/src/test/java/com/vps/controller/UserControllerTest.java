package com.vps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register and log in to obtain a JWT for authenticated requests
        Map<String, String> registerBody = Map.of(
                "fullName", "Test User",
                "email", "testuser@example.com",
                "password", "Password1!"
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated());

        Map<String, String> loginBody = Map.of(
                "email", "testuser@example.com",
                "password", "Password1!"
        );
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        // Extract token from {"success":true,"message":"...","data":{"token":"...",...}}
        jwtToken = objectMapper.readTree(responseBody).path("data").path("token").asText();
    }

    // (1) GET /api/v1/users/me without auth returns 403
    @Test
    void getProfile_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden());
    }

    // (2) GET /api/v1/users/me with valid JWT returns 200 with profile fields and NO passwordHash
    @Test
    void getProfile_authenticated_returns200WithProfile() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Test User"))
                .andExpect(jsonPath("$.data.email").value("testuser@example.com"))
                .andReturn();

        // Confirm no passwordHash field in the response
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).doesNotContain("passwordHash");
    }

    // (3) PUT /api/v1/users/me with valid JWT updates fullName in response
    @Test
    void updateProfile_validBody_returns200WithUpdatedName() throws Exception {
        Map<String, String> updateBody = Map.of(
                "fullName", "Updated Name",
                "phone", "9876543210"
        );

        MvcResult result = mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.phone").value("9876543210"))
                .andReturn();

        // Also confirm no passwordHash leaks in the update response
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).doesNotContain("passwordHash");
    }

    // (4) PUT /api/v1/users/me with blank fullName returns 400
    @Test
    void updateProfile_blankFullName_returns400() throws Exception {
        Map<String, String> updateBody = Map.of(
                "fullName", ""
        );

        mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isBadRequest());
    }

    // (5) Confirm no 'passwordHash' key appears in any profile response
    @Test
    void profileResponse_neverContainsPasswordHash() throws Exception {
        MvcResult getResult = mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(getResult.getResponse().getContentAsString()).doesNotContain("passwordHash");

        Map<String, String> updateBody = Map.of("fullName", "No Hash User");
        MvcResult putResult = mockMvc.perform(put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(putResult.getResponse().getContentAsString()).doesNotContain("passwordHash");
    }
}
