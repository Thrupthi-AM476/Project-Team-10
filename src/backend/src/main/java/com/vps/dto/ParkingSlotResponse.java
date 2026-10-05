package com.vps.dto;

import com.vps.entity.ParkingSlot.SlotStatus;
import com.vps.entity.ParkingSlot.SlotType;
import lombok.Data;

@Data
public class ParkingSlotResponse {
    private Long id;
    private String slotNumber;
    private SlotType slotType;
    private SlotStatus status;
    private String floor;
}
