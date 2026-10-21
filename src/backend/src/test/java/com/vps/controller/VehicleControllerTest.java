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
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** JWT for customer A (vehicle owner) */
    private String tokenA;

    /** JWT for customer B (a different customer) */
    private String tokenB;

    /** The ID of the vehicle created for customer A */
    private Long vehicleAId;

    private static final String PLATE_A = "KA01AB1234";

    @BeforeEach
    void setUp() throws Exception {
        // --- Register customer A ---
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Customer A",
                                "email", "custA@example.com",
                                "password", "Password1!"
                        ))))
                .andExpect(status().isCreated());

        tokenA = loginAndGetToken("custA@example.com", "Password1!");

        // --- Register customer B ---
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Customer B",
                                "email", "custB@example.com",
                                "password", "Password1!"
                        ))))
                .andExpect(status().isCreated());

        tokenB = loginAndGetToken("custB@example.com", "Password1!");

        // --- Register vehicle for customer A ---
        MvcResult vehicleResult = mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "licensePlate", PLATE_A,
                                "make", "Toyota",
                                "model", "Corolla",
                                "color", "White",
                                "vehicleType", "CAR"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        vehicleAId = objectMapper.readTree(
                vehicleResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();
    }

    // --- helper ---

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    // (1) POST /api/v1/vehicles — authenticated customer A with valid body — HTTP 201
    @Test
    void addVehicle_authenticated_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "licensePlate", "MH02CD5678",
                                "make", "Honda",
                                "model", "City",
                                "color", "Blue",
                                "vehicleType", "CAR"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    // (2) POST /api/v1/vehicles — duplicate plate — HTTP 400
    @Test
    void addVehicle_duplicatePlate_returns400() throws Exception {
        // Plate PLATE_A was already registered in setUp(); submitting again must fail
        mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "licensePlate", PLATE_A,
                                "make", "Ford",
                                "model", "Figo",
                                "color", "Red",
                                "vehicleType", "CAR"
                        ))))
                .andExpect(status().is4xxClientError());
    }

    // (3) GET /api/v1/vehicles — customer A — sees only own vehicles
    @Test
    void getVehicles_customerA_seesOnlyOwn() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[*].licensePlate", hasItem(PLATE_A)));
    }

    // (4) GET /api/v1/vehicles/{vehicleAId} as customer A — HTTP 200
    @Test
    void getVehicleById_ownerAccess_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/" + vehicleAId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(vehicleAId));
    }

    // (5) GET /api/v1/vehicles/{vehicleAId} as customer B — HTTP 403
    @Test
    void getVehicleById_otherCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/" + vehicleAId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    // (6) GET /api/v1/vehicles/search?plate=... as customer A — HTTP 200
    @Test
    void searchByPlate_ownerAccess_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/search")
                        .param("plate", PLATE_A)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.licensePlate").value(PLATE_A));
    }

    // (7) GET /api/v1/vehicles/search?plate=... as customer B — HTTP 403
    @Test
    void searchByPlate_otherCustomer_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/search")
                        .param("plate", PLATE_A)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    // (8) DELETE /api/v1/vehicles/{vehicleAId} as customer B — HTTP 403
    @Test
    void deleteVehicle_otherCustomer_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/vehicles/" + vehicleAId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    // (9) POST /api/v1/vehicles with lowercase plate — HTTP 201, stored plate is uppercase
    @Test
    void addVehicle_lowercasePlate_normalizedToUppercase() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/vehicles")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "licensePlate", "ka01ab9999",
                                "make", "Suzuki",
                                "model", "Swift",
                                "color", "Silver",
                                "vehicleType", "CAR"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        String plate = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("licensePlate").asText();
        assertThat(plate).isEqualTo("KA01AB9999");
    }
}
