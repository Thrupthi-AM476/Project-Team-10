package com.vps.controller;

import com.vps.dto.ApiResponse;
import com.vps.dto.VehicleRequest;
import com.vps.dto.VehicleResponse;
import com.vps.entity.User;
import com.vps.service.AuthService;
import com.vps.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final AuthService authService;

    // POST /api/v1/vehicles
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> addVehicle(
            @Valid @RequestBody VehicleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        VehicleResponse response = vehicleService.addVehicle(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle added successfully", response));
    }

    // GET /api/v1/vehicles — admin sees all, customer sees own
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getVehicles(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        List<VehicleResponse> vehicles;
        if (user.getRole() == User.Role.ADMINISTRATOR
                || user.getRole() == User.Role.PARKING_ATTENDANT) {
            vehicles = vehicleService.getAllVehicles();
        } else {
            vehicles = vehicleService.getVehiclesForUser(user.getId());
        }
        return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved", vehicles));
    }

    // GET /api/v1/vehicles/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(@PathVariable Long id) {
        // reuse search by id — find in all active vehicles
        VehicleResponse vehicle = vehicleService.getAllVehicles().stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
        return ResponseEntity.ok(ApiResponse.success("Vehicle retrieved", vehicle));
    }

    // GET /api/v1/vehicles/search?plate=KA01AB1234
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<VehicleResponse>> searchByPlate(
            @RequestParam String plate) {
        VehicleResponse vehicle = vehicleService.getByLicensePlate(plate);
        return ResponseEntity.ok(ApiResponse.success("Vehicle found", vehicle));
    }

    // PUT /api/v1/vehicles/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        VehicleResponse response = vehicleService.updateVehicle(id, request, user);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated", response));
    }

    // DELETE /api/v1/vehicles/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = authService.getUserByEmail(userDetails.getUsername());
        vehicleService.deactivateVehicle(id, user);
        return ResponseEntity.ok(ApiResponse.success("Vehicle deactivated", null));
    }
}
