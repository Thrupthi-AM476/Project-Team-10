package com.vps.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ParkingEntryResponse {
    private String ticketId;
    private String licensePlate;
    private String slotNumber;
    private String slotType;
    private LocalDateTime entryTime;
    private String status;
    private String message;
}
