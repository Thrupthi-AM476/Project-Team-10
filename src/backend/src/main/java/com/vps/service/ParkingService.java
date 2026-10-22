
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
    private final VehicleService vehicleService;
    private final AuditLogService auditLogService;
    private final ReservationRepository reservationRepository;

    private static final AtomicLong ticketCounter = new AtomicLong(1);

    // VPS-F-004: Slot availability
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

    // VPS-F-001 to VPS-F-003: Vehicle entry
    @Transactional
    public ParkingEntryResponse processEntry(
            ParkingEntryRequest request, User actor) {

        String plate = request.getLicensePlate().toUpperCase();

        Vehicle vehicle = vehicleService.getEntityByLicensePlate(plate);

        // Prevent a vehicle from being parked more than once
        if (recordRepository.findByVehicleIdAndStatus(
                vehicle.getId(), RecordStatus.ACTIVE).isPresent()) {
            throw new IllegalStateException(
                    "Vehicle " + plate + " is already parked");
        }

        // Find an available slot while respecting reservations
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

        // Mark slot occupied
        slot.setStatus(SlotStatus.OCCUPIED);
        slotRepository.save(slot);

        // Create parking record
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

    // VPS-F-007 to VPS-F-010: Vehicle exit, fee and payment
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

        LocalDateTime exitTime = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(
                record.getEntryTime(), exitTime);

        BigDecimal fee = calculateFee(
                record.getVehicle().getVehicleType(), minutes);

        // Process payment (mock — academic project)
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

        // Update parking record
        record.setExitTime(exitTime);
        record.setDurationMinutes((int) minutes);
        record.setFeeAmount(fee);
        record.setStatus(RecordStatus.COMPLETED);
        recordRepository.save(record);

        // Release parking slot
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

    public List<ParkingHistoryResponse> getParkingHistory(Long vehicleId) {
        return recordRepository
                .findByVehicleIdOrderByEntryTimeDesc(vehicleId)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    public List<ParkingHistoryResponse> getAllParkingHistory() {
        return recordRepository
                .findByStatusOrderByEntryTimeDesc(RecordStatus.COMPLETED)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // Fee calculation — VPS-F-007
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

    // Helpers
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

    private ParkingSlotResponse toSlotResponse(ParkingSlot s) {
        ParkingSlotResponse r = new ParkingSlotResponse();
        r.setId(s.getId());
        r.setSlotNumber(s.getSlotNumber());
        r.setSlotType(s.getSlotType());
        r.setStatus(s.getStatus());
        r.setFloor(s.getFloor());
        return r;
    }

    private ParkingHistoryResponse toHistoryResponse(ParkingRecord rec) {
        ParkingHistoryResponse r = new ParkingHistoryResponse();
        r.setId(rec.getId());
        r.setTicketId(rec.getTicketId());
        r.setLicensePlate(rec.getVehicle().getLicensePlate());
        r.setSlotNumber(rec.getSlot().getSlotNumber());
        r.setEntryTime(rec.getEntryTime());
        r.setExitTime(rec.getExitTime());
        r.setDurationMinutes(rec.getDurationMinutes());
        r.setFeeAmount(rec.getFeeAmount());
        r.setStatus(rec.getStatus().name());

        paymentRepository.findByParkingRecordId(rec.getId()).ifPresent(p -> {
            r.setPaymentMethod(p.getPaymentMethod().name());
            r.setPaymentStatus(p.getPaymentStatus().name());
        });

        return r;
    }
}
