package com.vps.dto;

import com.vps.entity.Vehicle.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RateScheduleRequest {

    @NotBlank(message = "Rate schedule name is required")
    @Size(max = 100, message = "Rate schedule name must be at most 100 characters")
    private String name;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.01", message = "Hourly rate must be greater than zero")
    private BigDecimal ratePerHour;

    @Min(value = 0, message = "Grace period cannot be negative")
    private int gracePeriodMinutes = 15;

    // Null means default active on create and unchanged on update.
    private Boolean active;
}
