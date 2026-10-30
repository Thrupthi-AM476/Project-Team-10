package com.vps.service;

import com.vps.dto.VehicleResponse;
import com.vps.entity.User;
import com.vps.entity.Vehicle;
import com.vps.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceListTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void getAllVehicles_includesActiveAndInactiveVehicles() {
        User owner = new User();
        owner.setId(1L);
        owner.setFullName("Vehicle Owner");
        owner.setRole(User.Role.CUSTOMER);

        Vehicle activeVehicle = makeVehicle(10L, "KA01AB1234", true, owner);
        Vehicle inactiveVehicle = makeVehicle(11L, "KA02CD5678", false, owner);

        when(vehicleRepository.findAll())
                .thenReturn(List.of(activeVehicle, inactiveVehicle));

        List<VehicleResponse> result = vehicleService.getAllVehicles();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(VehicleResponse::getLicensePlate)
                .containsExactly("KA01AB1234", "KA02CD5678");
        assertThat(result).extracting(VehicleResponse::isActive)
                .containsExactly(true, false);
        verify(vehicleRepository).findAll();
        verify(vehicleRepository, never()).findByIsActiveTrue();
    }

    private Vehicle makeVehicle(Long id, String plate, boolean active, User owner) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setLicensePlate(plate);
        vehicle.setVehicleType(Vehicle.VehicleType.CAR);
        vehicle.setUser(owner);
        vehicle.setActive(active);
        return vehicle;
    }
}
