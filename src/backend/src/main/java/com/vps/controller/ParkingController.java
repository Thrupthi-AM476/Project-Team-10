package com.vps.controller;

import com.vps.dto.*;
import com.vps.entity.User;
import com.vps.service.AuthService;
import com.vps.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // GET /api/v1/parking/slots — public (entry terminal display)
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

    // GET /api/v1/parking/active-session — customer's active parking session
    @GetMapping("/active-session")
    public ResponseEntity<ApiResponse<List<ParkingHistoryResponse>>> getActiveSession(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<ParkingHistoryResponse> active = parkingService.getActiveSessionsForUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Active session retrieved", active));
    }

    // POST /api/v1/parking/slots — staff/admin only
    @PostMapping("/slots")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','PARKING_ATTENDANT')")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> createSlot(
            @Valid @RequestBody ParkingSlotRequest request) {
        ParkingSlotResponse response = parkingService.createSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Slot created", response));
    }

    // PUT /api/v1/parking/slots/{id} — staff/admin only
    @PutMapping("/slots/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','PARKING_ATTENDANT')")
    public ResponseEntity<ApiResponse<ParkingSlotResponse>> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody ParkingSlotRequest request) {
        ParkingSlotResponse response = parkingService.updateSlot(id, request);
        return ResponseEntity.ok(ApiResponse.success("Slot updated", response));
    }

    // DELETE /api/v1/parking/slots/{id} — staff/admin only (marks out of service)
    @DeleteMapping("/slots/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR','PARKING_ATTENDANT')")
    public ResponseEntity<ApiResponse<Void>> deactivateSlot(@PathVariable Long id) {
        parkingService.deactivateSlot(id);
        return ResponseEntity.ok(ApiResponse.success("Slot marked out of service", null));
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

    // GET /api/v1/parking/history — own history for customers, all for staff
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ParkingHistoryResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<ParkingHistoryResponse> history;
        if (user.getRole() == User.Role.ADMINISTRATOR
                || user.getRole() == User.Role.PARKING_ATTENDANT) {
            history = parkingService.getAllParkingHistory();
        } else {
            history = parkingService.getParkingHistoryForUser(user.getId());
        }
        return ResponseEntity.ok(ApiResponse.success("History retrieved", history));
    }

    // GET /api/v1/parking/history/vehicle/{vehicleId} — ownership-aware
    @GetMapping("/history/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<List<ParkingHistoryResponse>>> getHistoryByVehicle(
            @PathVariable Long vehicleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<ParkingHistoryResponse> history = parkingService.getParkingHistory(vehicleId, user);
        return ResponseEntity.ok(ApiResponse.success("History retrieved", history));
    }
}
