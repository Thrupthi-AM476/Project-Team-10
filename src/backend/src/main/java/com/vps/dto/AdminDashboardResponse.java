package com.vps.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminDashboardResponse {
    private long totalSlots;
    private long availableSlots;
    private long occupiedSlots;
    private long reservedSlots;
    private long outOfServiceSlots;
    private long activeVehicles;
    private long totalUsersRegistered;
    private long totalCompletedTransactions;
    private BigDecimal revenueToday;
    private BigDecimal revenueThisMonth;
}
