package com.vps.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ParkingHistoryResponse {
    private Long id;
    private String ticketId;
    private String licensePlate;
    private String slotNumber;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Integer durationMinutes;
    private BigDecimal feeAmount;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
}
