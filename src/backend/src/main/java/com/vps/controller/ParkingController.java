package com.vps.controller;

import com.vps.dto.*;
import com.vps.entity.User;
import com.vps.service.AuthService;
import com.vps.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;
    private final AuthService authService;

    // GET /api/v1/parking/slots — public
    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<List<ParkingSlotResponse>>> getAllSlots() {
        return ResponseEntity.ok(
                ApiResponse.success("Slots retrieved", parkingService.getAllSlots()));
    }

    // GET /api/v1/parking/slots/available
    @GetMapping("/slots/available")
    public ResponseEntity<ApiResponse<List<ParkingSlotResponse>>> getAvailableSlots() {
        return ResponseEntity.ok(
                ApiResponse.success("Available slots retrieved",
                        parkingService.getAvailableSlots()));
    }

    // POST /api/v1/parking/entry
    @PostMapping("/entry")
    public ResponseEntity<ApiResponse<ParkingEntryResponse>> processEntry(
            @Valid @RequestBody ParkingEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        ParkingEntryResponse response = parkingService.processEntry(request, user);
        return ResponseEntity.ok(ApiResponse.success("Entry recorded", response));
    }

    // POST /api/v1/parking/exit
    @PostMapping("/exit")
    public ResponseEntity<ApiResponse<ParkingExitResponse>> processExit(
            @Valid @RequestBody ParkingExitRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        ParkingExitResponse response = parkingService.processExit(request, user);
        return ResponseEntity.ok(ApiResponse.success("Exit processed", response));
    }

    // GET /api/v1/parking/history — returns own history for customers,
    //     all history for admin/attendant
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ParkingHistoryResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<ParkingHistoryResponse> history;
        if (user.getRole() == User.Role.ADMINISTRATOR
                || user.getRole() == User.Role.PARKING_ATTENDANT) {
            history = parkingService.getAllParkingHistory();
        } else {
            // get first active vehicle for customer — simplified for academic scope
            history = parkingService.getAllParkingHistory().stream()
                    .filter(h -> {
                        try {
                            return parkingService.getParkingHistory(
                                    user.getId()).contains(h);
                        } catch (Exception e) {
                            return false;
                        }
                    }).toList();
        }
        return ResponseEntity.ok(ApiResponse.success("History retrieved", history));
    }

    // GET /api/v1/parking/history/vehicle/{vehicleId}
    @GetMapping("/history/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<List<ParkingHistoryResponse>>> getHistoryByVehicle(
            @PathVariable Long vehicleId) {
        List<ParkingHistoryResponse> history = parkingService.getParkingHistory(vehicleId);
        return ResponseEntity.ok(ApiResponse.success("History retrieved", history));
    }
}
