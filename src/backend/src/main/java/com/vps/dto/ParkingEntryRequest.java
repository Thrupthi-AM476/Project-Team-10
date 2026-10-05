package com.vps.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ParkingEntryRequest {

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    // Optional: prefer a specific slot type (REGULAR, DISABLED, EV)
    private String slotType;

    // How license plate was captured
    private String captureMethod = "MANUAL";
}
