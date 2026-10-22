package com.vps.dto;

import com.vps.entity.ParkingSlot.SlotType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParkingSlotRequest {

    @NotBlank(message = "Slot number is required")
    private String slotNumber;

    @NotNull(message = "Slot type is required")
    private SlotType slotType;

    private String floor = "G";

    // Optional: used for updates to change status
    private String status;
}
