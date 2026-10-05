package com.vps.controller;

import com.vps.dto.ApiResponse;
import com.vps.dto.ReservationRequest;
import com.vps.dto.ReservationResponse;
import com.vps.entity.User;
import com.vps.service.AuthService;
import com.vps.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final AuthService authService;

    // POST /api/v1/reservations
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        ReservationResponse response = reservationService.createReservation(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reservation created", response));
    }

    // GET /api/v1/reservations
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<ReservationResponse> reservations;
        if (user.getRole() == User.Role.ADMINISTRATOR) {
            reservations = reservationService.getAllReservations();
        } else {
            reservations = reservationService.getUserReservations(user.getId());
        }
        return ResponseEntity.ok(ApiResponse.success("Reservations retrieved", reservations));
    }

    // DELETE /api/v1/reservations/{id}  — cancel
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        ReservationResponse response = reservationService.cancelReservation(id, user);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled", response));
    }

    // POST /api/v1/reservations/expire — trigger expiry check (admin/attendant)
    @PostMapping("/expire")
    public ResponseEntity<ApiResponse<Void>> expireReservations() {
        reservationService.expireOverdueReservations();
        return ResponseEntity.ok(ApiResponse.success("Expired reservations updated", null));
    }
}
