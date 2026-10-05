package com.vps.repository;

import com.vps.entity.ParkingRecord;
import com.vps.entity.ParkingRecord.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingRecordRepository extends JpaRepository<ParkingRecord, Long> {
    Optional<ParkingRecord> findByTicketId(String ticketId);
    Optional<ParkingRecord> findByVehicleIdAndStatus(Long vehicleId, RecordStatus status);
    List<ParkingRecord> findByVehicleIdOrderByEntryTimeDesc(Long vehicleId);
    List<ParkingRecord> findByStatusOrderByEntryTimeDesc(RecordStatus status);
    List<ParkingRecord> findByEntryTimeBetweenOrderByEntryTimeDesc(LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(r) FROM ParkingRecord r WHERE r.status = 'ACTIVE'")
    long countActiveRecords();

    @Query("SELECT COALESCE(SUM(r.feeAmount), 0) FROM ParkingRecord r WHERE r.status = 'COMPLETED' AND r.exitTime >= :from AND r.exitTime <= :to")
    java.math.BigDecimal sumRevenueByDateRange(LocalDateTime from, LocalDateTime to);

    boolean existsBySlotIdAndStatus(Long slotId, RecordStatus status);
}
