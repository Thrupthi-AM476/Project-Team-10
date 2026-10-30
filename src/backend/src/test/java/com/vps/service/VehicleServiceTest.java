package com.vps.service;

import com.vps.dto.VehicleRequest;
import com.vps.dto.VehicleResponse;
import com.vps.entity.User;
import com.vps.entity.Vehicle;
import com.vps.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private VehicleService vehicleService;

    // --- helpers ---

    private User makeUser(Long id, User.Role role) {
        User u = new User();
        u.setId(id);
        u.setFullName("User " + id);
        u.setEmail("user" + id + "@example.com");
        u.setPasswordHash("$2a$hash");
        u.setRole(role);
        u.setActive(true);
        return u;
    }

    private Vehicle makeVehicle(Long vehicleId, User owner) {
        Vehicle v = new Vehicle();
        v.setId(vehicleId);
        v.setLicensePlate("KA01AB1234");
        v.setMake("Toyota");
        v.setModel("Corolla");
        v.setColor("White");
        v.setVehicleType(Vehicle.VehicleType.CAR);
        v.setUser(owner);
        v.setActive(true);
        return v;
    }

    private VehicleRequest makeRequest(String plate) {
        VehicleRequest r = new VehicleRequest();
        r.setLicensePlate(plate);
        r.setMake("Honda");
        r.setModel("City");
        r.setColor("Blue");
        r.setVehicleType(Vehicle.VehicleType.CAR);
        return r;
    }

    // --- addVehicle tests ---

    // (1) addVehicle_success — plate not duplicate, saved vehicle has uppercase plate and correct owner
    @Test
    void addVehicle_success() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        when(vehicleRepository.existsByLicensePlate("MH02CD5678")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId(10L);
            return v;
        });

        VehicleRequest req = makeRequest("mh02cd5678"); // lowercase input
        VehicleResponse resp = vehicleService.addVehicle(req, owner);

        assertThat(resp.getLicensePlate()).isEqualTo("MH02CD5678"); // normalized to uppercase
        assertThat(resp.getUserId()).isEqualTo(1L);
        verify(vehicleRepository).save(argThat(v -> "MH02CD5678".equals(v.getLicensePlate())));
    }

    // (2) addVehicle_duplicatePlate — existsByLicensePlate=true — throws IllegalArgumentException
    @Test
    void addVehicle_duplicatePlate() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        when(vehicleRepository.existsByLicensePlate("KA01AB1234")).thenReturn(true);

        VehicleRequest req = makeRequest("KA01AB1234");
        assertThatThrownBy(() -> vehicleService.addVehicle(req, owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    // --- getVehicleById tests ---

    // (3) getVehicleById_ownerCanSeeOwnVehicle — CUSTOMER, same id — returns response
    @Test
    void getVehicleById_ownerCanSeeOwnVehicle() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));

        VehicleResponse resp = vehicleService.getVehicleById(100L, owner);
        assertThat(resp.getId()).isEqualTo(100L);
    }

    // (4) getVehicleById_customerCannotSeeOtherVehicle — CUSTOMER, different id — throws SecurityException
    @Test
    void getVehicleById_customerCannotSeeOtherVehicle() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        User otherCustomer = makeUser(2L, User.Role.CUSTOMER);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> vehicleService.getVehicleById(100L, otherCustomer))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authorized");
    }

    // (5) getVehicleById_adminCanSeeAnyVehicle — ADMINISTRATOR — returns response
    @Test
    void getVehicleById_adminCanSeeAnyVehicle() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        User admin = makeUser(99L, User.Role.ADMINISTRATOR);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));

        VehicleResponse resp = vehicleService.getVehicleById(100L, admin);
        assertThat(resp.getId()).isEqualTo(100L);
    }

    // (6) getVehicleById_parkingAttendantCanSeeAnyVehicle — PARKING_ATTENDANT — returns response
    @Test
    void getVehicleById_parkingAttendantCanSeeAnyVehicle() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        User attendant = makeUser(50L, User.Role.PARKING_ATTENDANT);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));

        VehicleResponse resp = vehicleService.getVehicleById(100L, attendant);
        assertThat(resp.getId()).isEqualTo(100L);
    }

    // --- getByLicensePlate(String, User) tests ---

    // (7) getByLicensePlate_ownerSees — CUSTOMER owner — returns response
    @Test
    void getByLicensePlate_ownerSees() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findByLicensePlate("KA01AB1234")).thenReturn(Optional.of(vehicle));

        VehicleResponse resp = vehicleService.getByLicensePlate("KA01AB1234", owner);
        assertThat(resp.getLicensePlate()).isEqualTo("KA01AB1234");
    }

    // (8) getByLicensePlate_customerCannotSeeOther — different CUSTOMER — throws SecurityException
    @Test
    void getByLicensePlate_customerCannotSeeOther() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        User other = makeUser(2L, User.Role.CUSTOMER);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findByLicensePlate("KA01AB1234")).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> vehicleService.getByLicensePlate("KA01AB1234", other))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authorized");
    }

    // --- deactivateVehicle tests ---

    // (9) deactivateVehicle_ownerCanDeactivate — owner — no exception
    @Test
    void deactivateVehicle_ownerCanDeactivate() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        assertThatNoException().isThrownBy(() -> vehicleService.deactivateVehicle(100L, owner));
        assertThat(vehicle.isActive()).isFalse();
    }

    // (10) deactivateVehicle_parkingAttendantCanDeactivate — PARKING_ATTENDANT — no exception
    @Test
    void deactivateVehicle_parkingAttendantCanDeactivate() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        User attendant = makeUser(50L, User.Role.PARKING_ATTENDANT);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        assertThatNoException().isThrownBy(() -> vehicleService.deactivateVehicle(100L, attendant));
    }

    // (11) deactivateVehicle_customerCannotDeactivateOther — different CUSTOMER — throws SecurityException
    @Test
    void deactivateVehicle_customerCannotDeactivateOther() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        User other = makeUser(2L, User.Role.CUSTOMER);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> vehicleService.deactivateVehicle(100L, other))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authorized");
    }

    // --- updateVehicle tests ---

    // (12) updateVehicle_ownerCanUpdate — owner — updated successfully
    @Test
    void updateVehicle_ownerCanUpdate() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByLicensePlate("KA01AB9999")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleRequest req = makeRequest("KA01AB9999");
        VehicleResponse resp = vehicleService.updateVehicle(100L, req, owner);
        assertThat(resp.getLicensePlate()).isEqualTo("KA01AB9999");
    }

    // (13) updateVehicle_customerCannotUpdateOther — different CUSTOMER — throws SecurityException
    @Test
    void updateVehicle_customerCannotUpdateOther() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        User other = makeUser(2L, User.Role.CUSTOMER);
        Vehicle vehicle = makeVehicle(100L, owner);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));

        VehicleRequest req = makeRequest("KA01AB9999");
        assertThatThrownBy(() -> vehicleService.updateVehicle(100L, req, other))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Not authorized");
    }

    // Regression (#46): roles other than CUSTOMER must not bypass the ownership check
    @Test
    void getVehicleById_maintenanceTechnician_otherUsersVehicle_throwsSecurityException() {
        User owner = makeUser(1L, User.Role.CUSTOMER);
        User technician = makeUser(77L, User.Role.MAINTENANCE_TECHNICIAN);
        Vehicle vehicle = makeVehicle(10L, owner);
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> vehicleService.getVehicleById(10L, technician))
                .isInstanceOf(SecurityException.class);
    }
}
