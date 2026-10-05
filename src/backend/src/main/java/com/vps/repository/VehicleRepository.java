package com.vps.repository;

import com.vps.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    // "Find a vehicle by its number plate" (lookup)
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    // "Is this plate already registered?" (duplicate check)
    boolean existsByLicensePlate(String licensePlate);

    // "Show me all vehicles belonging to this user"
    List<Vehicle> findByUserId(Long userId);
}
