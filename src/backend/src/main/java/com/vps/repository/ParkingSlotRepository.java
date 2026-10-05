package com.vps.repository;

import com.vps.entity.ParkingSlot;
import com.vps.entity.ParkingSlot.SlotStatus;
import com.vps.entity.ParkingSlot.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    List<ParkingSlot> findByStatus(SlotStatus status);
    List<ParkingSlot> findBySlotType(SlotType slotType);
    List<ParkingSlot> findByStatusAndSlotType(SlotStatus status, SlotType slotType);
    Optional<ParkingSlot> findFirstByStatusAndSlotType(SlotStatus status, SlotType slotType);
    Optional<ParkingSlot> findFirstByStatus(SlotStatus status);
    long countByStatus(SlotStatus status);
    long countBySlotType(SlotType slotType);
}
