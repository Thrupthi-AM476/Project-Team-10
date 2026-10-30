package com.vps.service;

import com.vps.dto.*;
import com.vps.entity.*;
import com.vps.entity.ParkingRecord.RecordStatus;
import com.vps.entity.ParkingSlot.SlotStatus;
import com.vps.entity.ParkingSlot.SlotType;
import com.vps.entity.Payment.PaymentMethod;
import com.vps.entity.Payment.PaymentStatus;
import com.vps.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final ParkingSlotRepository slotRepository;
    private final ParkingRecordRepository recordRepository;
    private final PaymentRepository paymentRepository;
    private final RateScheduleRepository rateScheduleRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;
    private final AuditLogService auditLogService;
    private final ReservationRepository reservationRepository;

    private static final AtomicLong ticketCounter = new AtomicLong(1);

    // ---------------------------------------------------------------
    // VPS-F-004: Slot availability
    // ---------------------------------------------------------------

    public List<ParkingSlotResponse> getAllSlots() {
        return slotRepository.findAll().stream()
                .map(this::toSlotResponse)
                .collect(Collectors.toList());
    }

    public List<ParkingSlotResponse> getAvailableSlots() {
        LocalDateTime now = LocalDateTime.now();

        return slotRepository.findByStatus(SlotStatus.AVAILABLE).stream()
                .filter(slot -> !reservationRepository
                        .isSlotReservedAtTime(slot.getId(), now))
                .map(this::toSlotResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // VPS-F-005: Slot management (staff/admin)
    // ---------------------------------------------------------------

    @Transactional
    public ParkingSlotResponse createSlot(ParkingSlotRequest request) {
        if (slotRepository.findAll().stream()
                .anyMatch(s -> s.getSlotNumber()
                        .equalsIgnoreCase(request.getSlotNumber()))) {
            throw new IllegalArgumentException(
                    "Slot number already exists: " + request.getSlotNumber());
        }

        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber(request.getSlotNumber().toUpperCase());
        slot.setSlotType(request.getSlotType());
        slot.setFloor(request.getFloor() != null ? request.getFloor() : "G");
        slot.setStatus(SlotStatus.AVAILABLE);

        try {
            slotRepository.save(slot);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "Slot number already exists: " + request.getSlotNumber());
        }

        return toSlotResponse(slot);
    }

    @Transactional
    public ParkingSlotResponse updateSlot(
            Long slotId, ParkingSlotRequest request) {

        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Slot not found: " + slotId));

        if (request.getSlotType() != null) {
            slot.setSlotType(request.getSlotType());
        }

        if (request.getFloor() != null) {
            slot.setFloor(request.getFloor());
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {
            slot.setStatus(
                    SlotStatus.valueOf(request.getStatus().toUpperCase()));
        }

        slotRepository.save(slot);

        return toSlotResponse(slot);
    }

    @Transactional
    public void deactivateSlot(Long slotId) {
        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Slot not found: " + slotId));

        slot.setStatus(SlotStatus.OUT_OF_SERVICE);
        slotRepository.save(slot);
    }

    // ---------------------------------------------------------------
    // VPS-F-001 to VPS-F-003: Vehicle entry
    // ---------------------------------------------------------------

    @Transactional
    public ParkingEntryResponse processEntry(
            ParkingEntryRequest request, User actor) {

        String plate = request.getLicensePlate().toUpperCase();

        Vehicle vehicle = vehicleService.getEntityByLicensePlate(plate);

        // Customers may park only vehicles they own.
        if (!isStaff(actor)
                && !vehicle.getUser().getId().equals(actor.getId())) {
            throw new SecurityException(
                    "You can only park your own vehicle");
        }

        // Prevent duplicate active parking sessions.
        if (recordRepository.findByVehicleIdAndStatus(
                vehicle.getId(), RecordStatus.ACTIVE).isPresent()) {
            throw new IllegalStateException(
                    "Vehicle " + plate + " is already parked");
        }

        // Select an available slot while respecting reservations.
        SlotType preferredType = parseSlotType(request.getSlotType());
        LocalDateTime entryTime = LocalDateTime.now();

        List<ParkingSlot> preferredCandidates = preferredType != null
                ? slotRepository.findByStatusAndSlotType(
                        SlotStatus.AVAILABLE, preferredType)
                : List.of();

        List<ParkingSlot> allAvailable =
                slotRepository.findByStatus(SlotStatus.AVAILABLE);

        ParkingSlot slot = java.util.stream.Stream.concat(
                        preferredCandidates.stream(),
                        allAvailable.stream().filter(s ->
                                preferredType == null
                                        || s.getSlotType() != preferredType))
                .filter(s -> !reservationRepository
                        .isSlotReservedAtTime(s.getId(), entryTime))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Parking is full or all available slots are reserved for this time."));

        // Mark the selected slot occupied.
        slot.setStatus(SlotStatus.OCCUPIED);
        slotRepository.save(slot);

        // Create the parking record.
        ParkingRecord record = new ParkingRecord();
        record.setTicketId(generateTicketId());
        record.setVehicle(vehicle);
        record.setSlot(slot);
        record.setEntryTime(entryTime);
        record.setStatus(RecordStatus.ACTIVE);
        record.setEntryTerminal(request.getCaptureMethod());
        recordRepository.save(record);

        auditLogService.log(
                actor,
                "VEHICLE_ENTRY",
                "PARKING_RECORD",
                record.getId(),
                "Entry: " + plate + " -> Slot " + slot.getSlotNumber()
                        + " Ticket: " + record.getTicketId());

        ParkingEntryResponse resp = new ParkingEntryResponse();
        resp.setTicketId(record.getTicketId());
        resp.setLicensePlate(plate);
        resp.setSlotNumber(slot.getSlotNumber());
        resp.setSlotType(slot.getSlotType().name());
        resp.setEntryTime(record.getEntryTime());
        resp.setStatus("ENTRY_SUCCESS");
        resp.setMessage(
                "Vehicle entry recorded. Slot "
                        + slot.getSlotNumber() + " assigned.");

        return resp;
    }

    // ---------------------------------------------------------------
    // VPS-F-007 to VPS-F-010: Vehicle exit, fee and payment
    // ---------------------------------------------------------------

    @Transactional
    public ParkingExitResponse processExit(
            ParkingExitRequest request, User actor) {

        ParkingRecord record = recordRepository
                .findByTicketId(request.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Ticket not found: " + request.getTicketId()));

        if (record.getStatus() != RecordStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Ticket " + request.getTicketId()
                            + " is already completed or cancelled");
        }

        // Customers may exit only their own parking sessions.
        if (!isStaff(actor)
                && !record.getVehicle().getUser().getId()
                        .equals(actor.getId())) {
            throw new SecurityException(
                    "You can only process exit for your own vehicle");
        }

        LocalDateTime exitTime = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(
                record.getEntryTime(), exitTime);

        BigDecimal fee = calculateFee(
                record.getVehicle().getVehicleType(), minutes);

        // Mock payment processing for the academic project.
        PaymentMethod method = PaymentMethod.valueOf(
                request.getPaymentMethod().toUpperCase());

        Payment payment = new Payment();
        payment.setParkingRecord(record);
        payment.setAmount(fee);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef("TXN-" + System.currentTimeMillis());
        payment.setPaidAt(exitTime);
        paymentRepository.save(payment);

        // Update the parking record.
        record.setExitTime(exitTime);
        record.setDurationMinutes((int) minutes);
        record.setFeeAmount(fee);
        record.setStatus(RecordStatus.COMPLETED);
        recordRepository.save(record);

        // Release the parking slot.
        ParkingSlot slot = record.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(slot);

        auditLogService.log(
                actor,
                "VEHICLE_EXIT",
                "PARKING_RECORD",
                record.getId(),
                "Exit: " + record.getVehicle().getLicensePlate()
                        + " Duration: " + minutes + "min Fee: " + fee);

        String receiptId = "RCP-" + record.getTicketId();

        ParkingExitResponse resp = new ParkingExitResponse();
        resp.setTicketId(record.getTicketId());
        resp.setLicensePlate(record.getVehicle().getLicensePlate());
        resp.setSlotNumber(slot.getSlotNumber());
        resp.setEntryTime(record.getEntryTime());
        resp.setExitTime(exitTime);
        resp.setDurationMinutes((int) minutes);
        resp.setFeeAmount(fee);
        resp.setCurrency("INR");
        resp.setPaymentMethod(method.name());
        resp.setPaymentStatus("SUCCESS");
        resp.setReceiptId(receiptId);
        resp.setMessage(
                "Exit processed successfully. Receipt: " + receiptId);

        return resp;
    }

    // ---------------------------------------------------------------
    // Parking history
    // ---------------------------------------------------------------

    public List<ParkingHistoryResponse> getParkingHistory(Long vehicleId) {
        return recordRepository
                .findByVehicleIdOrderByEntryTimeDesc(vehicleId)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Ownership-aware vehicle history lookup.
     * Customers may only see history for their own vehicle.
     */
    public List<ParkingHistoryResponse> getParkingHistory(
            Long vehicleId, User requester) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Vehicle not found: " + vehicleId));

        if (!isStaff(requester)
                && !vehicle.getUser().getId().equals(requester.getId())) {
            throw new SecurityException(
                    "Not authorized to view history for this vehicle");
        }

        return recordRepository
                .findByVehicleIdOrderByEntryTimeDesc(vehicleId)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all parking history for a specific user across their vehicles.
     */
    public List<ParkingHistoryResponse> getParkingHistoryForUser(Long userId) {
        return recordRepository
                .findByVehicleUserIdOrderByEntryTimeDesc(userId)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get active parking sessions for a user.
     */
    public List<ParkingHistoryResponse> getActiveSessionsForUser(Long userId) {
        return recordRepository
                .findActiveByVehicleUserIdOrderByEntryTimeDesc(userId)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    /** Only administrators and parking attendants may act on other users' records. */
    private boolean isStaff(User user) {
        return user.getRole() == User.Role.ADMINISTRATOR
                || user.getRole() == User.Role.PARKING_ATTENDANT;
    }

    public List<ParkingHistoryResponse> getAllParkingHistory() {
        return recordRepository
                .findByStatusOrderByEntryTimeDesc(RecordStatus.COMPLETED)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Fee calculation — VPS-F-007
    // ---------------------------------------------------------------

    private BigDecimal calculateFee(
            Vehicle.VehicleType vehicleType, long durationMinutes) {

        RateSchedule rate = rateScheduleRepository
                .findFirstByVehicleTypeAndIsActiveTrue(vehicleType)
                .orElseThrow(() -> new IllegalStateException(
                        "No rate schedule found for vehicle type: "
                                + vehicleType));

        long billableMinutes = Math.max(
                0, durationMinutes - rate.getGracePeriodMinutes());

        if (billableMinutes == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal ratePerMinute = rate.getRatePerHour()
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        return ratePerMinute
                .multiply(BigDecimal.valueOf(billableMinutes))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private String generateTicketId() {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return "TKT-" + date + "-"
                + String.format("%05d", ticketCounter.getAndIncrement());
    }

    private SlotType parseSlotType(String slotType) {
        if (slotType == null || slotType.isBlank()) {
            return null;
        }

        try {
            return SlotType.valueOf(slotType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ParkingSlotResponse toSlotResponse(ParkingSlot slot) {
        ParkingSlotResponse response = new ParkingSlotResponse();
        response.setId(slot.getId());
        response.setSlotNumber(slot.getSlotNumber());
        response.setSlotType(slot.getSlotType());
        response.setStatus(slot.getStatus());
        response.setFloor(slot.getFloor());
        return response;
    }

    /** Public wrapper for AdminService to reuse the history mapper. */
    public ParkingHistoryResponse toHistoryResponsePublic(ParkingRecord record) {
        return toHistoryResponse(record);
    }

    private ParkingHistoryResponse toHistoryResponse(ParkingRecord record) {
        ParkingHistoryResponse response = new ParkingHistoryResponse();
        response.setId(record.getId());
        response.setTicketId(record.getTicketId());
        response.setLicensePlate(record.getVehicle().getLicensePlate());
        response.setSlotNumber(record.getSlot().getSlotNumber());
        response.setEntryTime(record.getEntryTime());
        response.setExitTime(record.getExitTime());
        response.setDurationMinutes(record.getDurationMinutes());
        response.setFeeAmount(record.getFeeAmount());
        response.setStatus(record.getStatus().name());

        paymentRepository.findByParkingRecordId(record.getId()).ifPresent(payment -> {
            response.setPaymentMethod(payment.getPaymentMethod().name());
            response.setPaymentStatus(payment.getPaymentStatus().name());
        });

        return response;
    }
}
