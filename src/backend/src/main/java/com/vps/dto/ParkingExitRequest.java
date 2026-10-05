package com.vps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParkingExitRequest {

    @NotBlank(message = "Ticket ID is required")
    private String ticketId;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // CASH, CARD, DIGITAL_WALLET
}
