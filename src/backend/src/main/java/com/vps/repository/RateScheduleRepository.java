package com.vps.repository;

import com.vps.entity.RateSchedule;
import com.vps.entity.Vehicle.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RateScheduleRepository extends JpaRepository<RateSchedule, Long> {
    Optional<RateSchedule> findFirstByVehicleTypeAndIsActiveTrue(VehicleType vehicleType);
    List<RateSchedule> findByIsActiveTrue();
}
