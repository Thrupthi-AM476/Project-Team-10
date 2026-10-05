package com.vps.dto;

import com.vps.entity.Reservation.ReservationStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long vehicleId;
    private String licensePlate;
    private String slotNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private LocalDateTime createdAt;
}
