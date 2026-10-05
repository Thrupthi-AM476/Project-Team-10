package com.vps.dto;

import com.vps.entity.Vehicle.VehicleType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VehicleResponse {
    private Long id;
    private String licensePlate;
    private String make;
    private String model;
    private String color;
    private VehicleType vehicleType;
    private Long userId;
    private String ownerName;
    private boolean isActive;
    private LocalDateTime createdAt;
}
