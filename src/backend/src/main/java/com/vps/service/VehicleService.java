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

    /**
     * Return active and inactive vehicles to staff so the oversight table can
     * display the status of every registered vehicle. Customer queries remain
     * restricted to the customer's active vehicles by getVehiclesForUser.
     */
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Ownership-aware lookup by ID.
     * CUSTOMER may only see their own vehicle; ADMINISTRATOR and PARKING_ATTENDANT may see any.
     */
    public VehicleResponse getVehicleById(Long vehicleId, User requester) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        if (!isStaff(requester)
                && !vehicle.getUser().getId().equals(requester.getId())) {
            throw new SecurityException("Not authorized to view this vehicle");
        }

        return toResponse(vehicle);
    }

    /**
     * Ownership-aware lookup by plate.
     * Preserves the zero-argument overload used by ParkingService / ReservationService.
     */
    public VehicleResponse getByLicensePlate(String licensePlate, User requester) {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found: " + licensePlate));

        if (!isStaff(requester)
                && !vehicle.getUser().getId().equals(requester.getId())) {
            throw new SecurityException("Not authorized to view this vehicle");
        }

        return toResponse(vehicle);
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
                && requester.getRole() != User.Role.ADMINISTRATOR
                && requester.getRole() != User.Role.PARKING_ATTENDANT) {
            throw new SecurityException("Not authorized to delete this vehicle");
        }

        vehicle.setActive(false);
        vehicleRepository.save(vehicle);

        auditLogService.log(requester, "VEHICLE_DEACTIVATED", "VEHICLE", vehicle.getId(),
                "Vehicle deactivated: " + vehicle.getLicensePlate());
    }

    public VehicleResponse getByLicensePlate(String licensePlate) {
        Vehicle vehicle = vehicleRepository.findByLicensePlate(licensePlate.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found: " + licensePlate));
        return toResponse(vehicle);
    }

    public Vehicle getEntityByLicensePlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found: " + licensePlate));
    }

    /** Only administrators and parking attendants may act on other users' vehicles. */
    private boolean isStaff(User user) {
        return user.getRole() == User.Role.ADMINISTRATOR
                || user.getRole() == User.Role.PARKING_ATTENDANT;
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setLicensePlate(vehicle.getLicensePlate());
        response.setMake(vehicle.getMake());
        response.setModel(vehicle.getModel());
        response.setColor(vehicle.getColor());
        response.setVehicleType(vehicle.getVehicleType());
        response.setUserId(vehicle.getUser().getId());
        response.setOwnerName(vehicle.getUser().getFullName());
        response.setActive(vehicle.isActive());
        response.setCreatedAt(vehicle.getCreatedAt());
        return response;
    }
}
