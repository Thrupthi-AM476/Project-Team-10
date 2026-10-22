package com.vps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vps.entity.User;
import com.vps.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setUp() throws Exception {
        // Create admin user directly (registration always assigns CUSTOMER)
        User admin = new User();
        admin.setFullName("Test Admin");
        admin.setEmail("admin@vps-test.com");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123!"));
        admin.setRole(User.Role.ADMINISTRATOR);
        admin.setActive(true);
        userRepository.save(admin);

        adminToken = loginAndGetToken("admin@vps-test.com", "Admin@123!");

        // Register a customer
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fullName", "Regular User",
                        "email", "user@vps-test.com",
                        "password", "Password1!"))))
                .andExpect(status().isCreated());
        customerToken = loginAndGetToken("user@vps-test.com", "Password1!");
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    // (1) Admin can access dashboard
    @Test
    void getDashboard_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSlots").exists());
    }

    // (2) Customer cannot access admin dashboard
    @Test
    void getDashboard_customer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // (3) Unauthenticated cannot access admin dashboard
    @Test
    void getDashboard_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    // (4) Admin can view rate schedules
    @Test
    void getRates_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/rates")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // (5) Admin can create a rate schedule (VPS-F-014)
    @Test
    void createRate_admin_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/admin/rates")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Peak Rate",
                        "vehicleType", "CAR",
                        "ratePerHour", "50.00",
                        "gracePeriodMinutes", "10",
                        "active", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Peak Rate"));
    }

    // (6) Admin can update a rate schedule
    @Test
    void updateRate_admin_returns200() throws Exception {
        // Create first
        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/rates")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Standard Rate",
                        "vehicleType", "CAR",
                        "ratePerHour", "30.00",
                        "gracePeriodMinutes", "15",
                        "active", true))))
                .andExpect(status().isCreated())
                .andReturn();

        Long rateId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Update
        mockMvc.perform(put("/api/v1/admin/rates/" + rateId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Updated Rate",
                        "vehicleType", "CAR",
                        "ratePerHour", "40.00",
                        "gracePeriodMinutes", "10",
                        "active", true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Rate"))
                .andExpect(jsonPath("$.data.ratePerHour").value(40.00));
    }

    // (7) Admin can view reports
    @Test
    void getReport_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/reports")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // (8) Customer cannot create rate schedules
    @Test
    void createRate_customer_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/rates")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "name", "Hacked Rate",
                        "vehicleType", "CAR",
                        "ratePerHour", "0.01",
                        "gracePeriodMinutes", "0",
                        "active", true))))
                .andExpect(status().isForbidden());
    }
}
