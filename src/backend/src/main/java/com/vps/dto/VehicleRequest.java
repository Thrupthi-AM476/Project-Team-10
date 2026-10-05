package com.vps.dto;

import com.vps.entity.Vehicle.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VehicleRequest {

    @NotBlank(message = "License plate is required")
    @Pattern(regexp = "^[A-Z0-9\\-]{2,20}$", message = "Invalid license plate format")
    private String licensePlate;

    private String make;
    private String model;
    private String color;
    private VehicleType vehicleType = VehicleType.CAR;
}
