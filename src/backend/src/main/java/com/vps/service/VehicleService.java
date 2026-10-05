package com.vps.service;

import com.vps.dto.VehicleRequest;
import com.vps.dto.VehicleResponse;
import com.vps.entity.User;
import com.vps.entity.Vehicle;
import com.vps.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLogService;

    public VehicleResponse addVehicle(VehicleRequest request, User owner) {
        // VPS-F-001: prevent duplicate license plates
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate().toUpperCase())) {
            throw new IllegalArgumentException(
                    "Vehicle with license plate " + request.getLicensePlate() + " already exists");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(request.getLicensePlate().toUpperCase());
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setColor(request.getColor());
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setUser(owner);

        vehicleRepository.save(vehicle);
        auditLogService.log(owner, "VEHICLE_ADDED", "VEHICLE", vehicle.getId(),
                "Vehicle added: " + vehicle.getLicensePlate());

        return toResponse(vehicle);
    }

    public List<VehicleResponse> getVehiclesForUser(Long userId) {
        return vehicleRepository.findByUserIdAndIsActiveTrue(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findByIsActiveTrue()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public VehicleResponse updateVehicle(Long vehicleId, VehicleRequest request, User requester) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        // Only owner or admin can update
        if (!vehicle.getUser().getId().equals(requester.getId())
                && requester.getRole() != User.Role.ADMINISTRATOR) {
            throw new SecurityException("Not authorized to update this vehicle");
        }

        // If license plate changed, check for duplicate
        String newPlate = request.getLicensePlate().toUpperCase();
        if (!vehicle.getLicensePlate().equals(newPlate)
                && vehicleRepository.existsByLicensePlate(newPlate)) {
            throw new IllegalArgumentException("License plate " + newPlate + " already in use");
        }

        vehicle.setLicensePlate(newPlate);
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setColor(request.getColor());
        vehicle.setVehicleType(request.getVehicleType());
        vehicleRepository.save(vehicle);

        auditLogService.log(requester, "VEHICLE_UPDATED", "VEHICLE", vehicle.getId(),
                "Vehicle updated: " + vehicle.getLicensePlate());
        return toResponse(vehicle);
    }

    public void deactivateVehicle(Long vehicleId, User requester) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        if (!vehicle.getUser().getId().equals(requester.getId())
                && requester.getRole() != User.Role.ADMINISTRATOR) {
            throw new SecurityException("Not authorized to delete this vehicle");
        }

        vehicle.setActive(false);
        vehicleRepository.save(vehicle);

        auditLogService.log(requester, "VEHICLE_DEACTIVATED", "VEHICLE", vehicle.getId(),
                "Vehicle deactivated: " + vehicle.getLicensePlate());
    }

    public VehicleResponse getByLicensePlate(String licensePlate) {
        Vehicle v = vehicleRepository.findByLicensePlate(licensePlate.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found: " + licensePlate));
        return toResponse(v);
    }

    public Vehicle getEntityByLicensePlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found: " + licensePlate));
    }

    private VehicleResponse toResponse(Vehicle v) {
        VehicleResponse r = new VehicleResponse();
        r.setId(v.getId());
        r.setLicensePlate(v.getLicensePlate());
        r.setMake(v.getMake());
        r.setModel(v.getModel());
        r.setColor(v.getColor());
        r.setVehicleType(v.getVehicleType());
        r.setUserId(v.getUser().getId());
        r.setOwnerName(v.getUser().getFullName());
        r.setActive(v.isActive());
        r.setCreatedAt(v.getCreatedAt());
        return r;
    }
}
