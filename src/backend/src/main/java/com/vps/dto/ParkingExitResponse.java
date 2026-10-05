package com.vps.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ParkingExitResponse {
    private String ticketId;
    private String licensePlate;
    private String slotNumber;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private int durationMinutes;
    private BigDecimal feeAmount;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
    private String receiptId;
    private String message;
}
