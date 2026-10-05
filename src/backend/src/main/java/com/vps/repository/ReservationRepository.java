package com.vps.repository;

import com.vps.entity.Reservation;
import com.vps.entity.Reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdOrderByStartTimeDesc(Long userId);
    List<Reservation> findByStatus(ReservationStatus status);

    // Find expired reservations that are still CONFIRMED or PENDING
    @Query("SELECT r FROM Reservation r WHERE r.status IN ('PENDING', 'CONFIRMED') AND r.endTime < :now")
    List<Reservation> findExpiredReservations(LocalDateTime now);

    // Check if a slot is already reserved in a given time window
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slot.id = :slotId AND r.status IN ('PENDING','CONFIRMED') AND r.startTime < :end AND r.endTime > :start")
    boolean isSlotReservedInWindow(Long slotId, LocalDateTime start, LocalDateTime end);
}
