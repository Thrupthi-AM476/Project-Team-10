package com.vps.service;

import com.vps.dto.ReservationRequest;
import com.vps.dto.ReservationResponse;
import com.vps.entity.*;
import com.vps.entity.ParkingSlot.SlotStatus;
import com.vps.entity.ParkingSlot.SlotType;
import com.vps.entity.Reservation.ReservationStatus;
import com.vps.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ParkingSlotRepository slotRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLogService;

    // VPS-F-011: Create reservation
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, User user) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Vehicle does not belong to this user");
        }

        // Find an available slot for the time window
        SlotType preferred = parseSlotType(request.getSlotType());
        List<ParkingSlot> candidates = preferred != null
                ? slotRepository.findByStatusAndSlotType(SlotStatus.AVAILABLE, preferred)
                : slotRepository.findByStatus(SlotStatus.AVAILABLE);

        ParkingSlot slot = candidates.stream()
                .filter(s -> !reservationRepository.isSlotReservedInWindow(
                        s.getId(), request.getStartTime(), request.getEndTime()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No available slots for the requested time window"));

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setVehicle(vehicle);
        reservation.setSlot(slot);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        auditLogService.log(user, "RESERVATION_CREATED", "RESERVATION", reservation.getId(),
                "Reservation for " + vehicle.getLicensePlate()
                        + " slot " + slot.getSlotNumber());

        return toResponse(reservation);
    }

    public List<ReservationResponse> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByStartTimeDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    // VPS-F-012: Cancel reservation
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, User user) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (!reservation.getUser().getId().equals(user.getId())
                && user.getRole() != User.Role.ADMINISTRATOR) {
            throw new SecurityException("Not authorized to cancel this reservation");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Reservation is already " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        auditLogService.log(user, "RESERVATION_CANCELLED", "RESERVATION", reservation.getId(),
                "Reservation cancelled: " + reservationId);

        return toResponse(reservation);
    }

    // VPS-F-012: Expire overdue reservations (called periodically or on demand)
    @Transactional
    public void expireOverdueReservations() {
        List<Reservation> expired = reservationRepository
                .findExpiredReservations(LocalDateTime.now());
        for (Reservation r : expired) {
            r.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(r);
            auditLogService.log(null, "RESERVATION_EXPIRED", "RESERVATION", r.getId(),
                    "Reservation expired: " + r.getId());
        }
    }

    private ReservationResponse toResponse(Reservation r) {
        ReservationResponse resp = new ReservationResponse();
        resp.setId(r.getId());
        resp.setUserId(r.getUser().getId());
        resp.setUserName(r.getUser().getFullName());
        resp.setVehicleId(r.getVehicle().getId());
        resp.setLicensePlate(r.getVehicle().getLicensePlate());
        resp.setSlotNumber(r.getSlot() != null ? r.getSlot().getSlotNumber() : null);
        resp.setStartTime(r.getStartTime());
        resp.setEndTime(r.getEndTime());
        resp.setStatus(r.getStatus());
        resp.setCreatedAt(r.getCreatedAt());
        return resp;
    }

    private SlotType parseSlotType(String slotType) {
        if (slotType == null || slotType.isBlank()) return null;
        try { return SlotType.valueOf(slotType.toUpperCase()); }
        catch (IllegalArgumentException e) { return null; }
    }
}
