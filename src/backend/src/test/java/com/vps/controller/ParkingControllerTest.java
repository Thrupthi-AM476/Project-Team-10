package com.vps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vps.entity.ParkingSlot;
import com.vps.entity.RateSchedule;
import com.vps.entity.Vehicle.VehicleType;
import com.vps.repository.ParkingSlotRepository;
import com.vps.repository.RateScheduleRepository;
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

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ParkingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ParkingSlotRepository slotRepository;
    @Autowired private RateScheduleRepository rateScheduleRepository;

    private String customerToken;
    private Long vehicleId;

    @BeforeEach
    void setUp() throws Exception {
        // Register a customer
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "fullName", "Park Customer",
                        "email", "park@example.com",
                        "password", "Password1!"))))
                .andExpect(status().isCreated());

        customerToken = loginAndGetToken("park@example.com", "Password1!");

        // Create a vehicle for the customer
        MvcResult vResult = mockMvc.perform(post("/api/v1/vehicles")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "licensePlate", "KA05PG9999",
                        "make", "Honda", "model", "City",
                        "color", "Black", "vehicleType", "CAR"))))
                .andExpect(status().isCreated())
                .andReturn();
        vehicleId = objectMapper.readTree(vResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Seed a parking slot for tests
        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber("T-01");
        slot.setSlotType(ParkingSlot.SlotType.REGULAR);
        slot.setStatus(ParkingSlot.SlotStatus.AVAILABLE);
        slot.setFloor("G");
        slotRepository.save(slot);

        // Seed a rate schedule for fee calculation
        RateSchedule rate = new RateSchedule();
        rate.setName("Test Rate");
        rate.setVehicleType(VehicleType.CAR);
        rate.setRatePerHour(new BigDecimal("30.00"));
        rate.setGracePeriodMinutes(15);
        rate.setActive(true);
        rateScheduleRepository.save(rate);
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

    // (1) Public can view all slots
    @Test
    void getAllSlots_public_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/parking/slots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // (2) Public can view available slots
    @Test
    void getAvailableSlots_public_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/parking/slots/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // (3) Customer can process entry for their own vehicle
    @Test
    void processEntry_ownVehicle_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/parking/entry")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "licensePlate", "KA05PG9999"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ticketId").isNotEmpty())
                .andExpect(jsonPath("$.data.slotNumber").isNotEmpty());
    }

    // (4) Duplicate active session is rejected
    @Test
    void processEntry_alreadyParked_returns403() throws Exception {
        // First entry
        mockMvc.perform(post("/api/v1/parking/entry")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "licensePlate", "KA05PG9999"))))
                .andExpect(status().isOk());

        // Second entry — should be rejected
        mockMvc.perform(post("/api/v1/parking/entry")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "licensePlate", "KA05PG9999"))))
                .andExpect(status().isForbidden());
    }

    // (5) Customer can process exit and get fee
    @Test
    void processExit_returns200WithFee() throws Exception {
        // Entry first
        MvcResult entryResult = mockMvc.perform(post("/api/v1/parking/entry")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "licensePlate", "KA05PG9999"))))
                .andExpect(status().isOk())
                .andReturn();

        String ticketId = objectMapper.readTree(entryResult.getResponse().getContentAsString())
                .path("data").path("ticketId").asText();
        assertThat(ticketId).isNotEmpty();

        // Exit
        mockMvc.perform(post("/api/v1/parking/exit")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "ticketId", ticketId,
                        "paymentMethod", "CASH"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receiptId").isNotEmpty())
                .andExpect(jsonPath("$.data.paymentStatus").value("SUCCESS"));
    }

    // (6) Customer can view their own parking history
    @Test
    void getHistory_customer_returnsOwn() throws Exception {
        // Create a parking session first
        mockMvc.perform(post("/api/v1/parking/entry")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "licensePlate", "KA05PG9999"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/parking/history")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    // (7) Customer can get their active session
    @Test
    void getActiveSession_returnsActiveRecord() throws Exception {
        mockMvc.perform(post("/api/v1/parking/entry")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "licensePlate", "KA05PG9999"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/parking/active-session")
                .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", org.hamcrest.Matchers.hasSize(1)));
    }

    // (8) Unauthenticated entry is rejected
    @Test
    void processEntry_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/parking/entry")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "licensePlate", "KA05PG9999"))))
                .andExpect(status().isForbidden());
    }
}
