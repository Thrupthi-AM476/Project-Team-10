package com.vps.dto;

import com.vps.entity.Vehicle.VehicleType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RateScheduleResponse {
    private Long id;
    private String name;
    private VehicleType vehicleType;
    private BigDecimal ratePerHour;
    private int gracePeriodMinutes;
    private boolean active;
    private LocalDateTime createdAt;
}
