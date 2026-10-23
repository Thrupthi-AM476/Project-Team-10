package com.vps.dto;

import com.vps.entity.Vehicle.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RateScheduleRequest {

    @NotBlank(message = "Rate name is required")
    private String name;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotNull(message = "Rate per hour is required")
    @DecimalMin(value = "0.01", message = "Rate per hour must be greater than 0")
    private BigDecimal ratePerHour;

    @Min(value = 0, message = "Grace period cannot be negative")
    private int gracePeriodMinutes = 15;

    private boolean active = true;
}
