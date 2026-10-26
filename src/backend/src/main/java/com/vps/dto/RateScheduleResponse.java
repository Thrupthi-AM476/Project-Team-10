```java
package com.vps.dto;

import com.vps.entity.RateSchedule;
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
    private LocalDateTime updatedAt;

    public static RateScheduleResponse from(RateSchedule schedule) {
        RateScheduleResponse response = new RateScheduleResponse();
        response.setId(schedule.getId());
        response.setName(schedule.getName());
        response.setVehicleType(schedule.getVehicleType());
        response.setRatePerHour(schedule.getRatePerHour());
        response.setGracePeriodMinutes(schedule.getGracePeriodMinutes());
        response.setActive(schedule.isActive());
        response.setCreatedAt(schedule.getCreatedAt());
        response.setUpdatedAt(schedule.getUpdatedAt());
        return response;
    }
}
```