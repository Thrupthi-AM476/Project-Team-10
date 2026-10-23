package com.vps.service;

import com.vps.dto.ParkingEntryRequest;
import com.vps.dto.ParkingEntryResponse;
import com.vps.dto.ParkingExitRequest;
import com.vps.dto.ParkingExitResponse;
import com.vps.entity.*;
import com.vps.entity.ParkingRecord.RecordStatus;
import com.vps.entity.ParkingSlot.SlotStatus;
import com.vps.entity.Payment.PaymentStatus;
import com.vps.entity.Vehicle.VehicleType;
import com.vps.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock private ParkingSlotRepository slotRepository;
    @Mock private ParkingRecordRepository recordRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private RateScheduleRepository rateScheduleRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private VehicleService vehicleService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private ParkingService parkingService;

    private User customer;
    private User otherCustomer;
    private Vehicle vehicle;
    private ParkingSlot slot;
    private RateSchedule rate;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setRole(User.Role.CUSTOMER);

        otherCustomer = new User();
        otherCustomer.setId(2L);
        otherCustomer.setRole(User.Role.CUSTOMER);

        vehicle = new Vehicle();
        vehicle.setId(10L);
        vehicle.setLicensePlate("KA01AB1234");
        vehicle.setVehicleType(VehicleType.CAR);
        vehicle.setUser(customer);

        slot = new ParkingSlot();
        slot.setId(5L);
        slot.setSlotNumber("A-01");
        slot.setSlotType(ParkingSlot.SlotType.REGULAR);
        slot.setStatus(SlotStatus.AVAILABLE);

        rate = new RateSchedule();
        rate.setRatePerHour(new BigDecimal("30.00"));
        rate.setGracePeriodMinutes(15);
        rate.setVehicleType(VehicleType.CAR);
    }

    // (1) Customer can park their own vehicle
    @Test
    void processEntry_ownVehicle_succeeds() {
        ParkingEntryRequest req = new ParkingEntryRequest();
        req.setLicensePlate("ka01ab1234"); // lowercase — should be normalized

        when(vehicleService.getEntityByLicensePlate("KA01AB1234")).thenReturn(vehicle);
        when(recordRepository.findByVehicleIdAndStatus(10L, RecordStatus.ACTIVE)).thenReturn(Optional.empty());
        when(slotRepository.findFirstByStatus(SlotStatus.AVAILABLE)).thenReturn(Optional.of(slot));

        ParkingEntryResponse resp = parkingService.processEntry(req, customer);

        assertThat(resp.getStatus()).isEqualTo("ENTRY_SUCCESS");
        assertThat(resp.getLicensePlate()).isEqualTo("KA01AB1234");
        assertThat(resp.getTicketId()).isNotEmpty();
        verify(slotRepository).save(argThat(s -> s.getStatus() == SlotStatus.OCCUPIED));
    }

    // (2) Customer cannot park another user's vehicle
    @Test
    void processEntry_otherUserVehicle_throwsSecurityException() {
        ParkingEntryRequest req = new ParkingEntryRequest();
        req.setLicensePlate("KA01AB1234");

        when(vehicleService.getEntityByLicensePlate("KA01AB1234")).thenReturn(vehicle);

        assertThatThrownBy(() -> parkingService.processEntry(req, otherCustomer))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("own vehicle");
    }

    // (3) Duplicate active session is rejected
    @Test
    void processEntry_alreadyParked_throwsIllegalStateException() {
        ParkingEntryRequest req = new ParkingEntryRequest();
        req.setLicensePlate("KA01AB1234");

        ParkingRecord existing = new ParkingRecord();
        existing.setStatus(RecordStatus.ACTIVE);

        when(vehicleService.getEntityByLicensePlate("KA01AB1234")).thenReturn(vehicle);
        when(recordRepository.findByVehicleIdAndStatus(10L, RecordStatus.ACTIVE)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> parkingService.processEntry(req, customer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already parked");
    }

    // (4) No slots available — parking full
    @Test
    void processEntry_noSlots_throwsIllegalStateException() {
        ParkingEntryRequest req = new ParkingEntryRequest();
        req.setLicensePlate("KA01AB1234");

        when(vehicleService.getEntityByLicensePlate("KA01AB1234")).thenReturn(vehicle);
        when(recordRepository.findByVehicleIdAndStatus(10L, RecordStatus.ACTIVE)).thenReturn(Optional.empty());
        when(slotRepository.findFirstByStatus(SlotStatus.AVAILABLE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.processEntry(req, customer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("full");
    }

    // (5) Customer can exit their own parking session
    @Test
    void processExit_ownVehicle_succeeds() {
        ParkingExitRequest req = new ParkingExitRequest();
        req.setTicketId("TKT-20261022-00001");
        req.setPaymentMethod("CASH");

        ParkingRecord record = new ParkingRecord();
        record.setId(1L);
        record.setTicketId("TKT-20261022-00001");
        record.setVehicle(vehicle);
        record.setSlot(slot);
        record.setEntryTime(LocalDateTime.now().minusMinutes(60));
        record.setStatus(RecordStatus.ACTIVE);

        when(recordRepository.findByTicketId("TKT-20261022-00001")).thenReturn(Optional.of(record));
        when(rateScheduleRepository.findFirstByVehicleTypeAndIsActiveTrue(VehicleType.CAR)).thenReturn(Optional.of(rate));

        ParkingExitResponse resp = parkingService.processExit(req, customer);

        assertThat(resp.getReceiptId()).isNotEmpty();
        assertThat(resp.getPaymentStatus()).isEqualTo("SUCCESS");
        verify(slotRepository).save(argThat(s -> s.getStatus() == SlotStatus.AVAILABLE));
    }

    // (6) Customer cannot exit another user's session
    @Test
    void processExit_otherUserVehicle_throwsSecurityException() {
        ParkingExitRequest req = new ParkingExitRequest();
        req.setTicketId("TKT-20261022-00001");
        req.setPaymentMethod("CASH");

        ParkingRecord record = new ParkingRecord();
        record.setId(1L);
        record.setTicketId("TKT-20261022-00001");
        record.setVehicle(vehicle);
        record.setSlot(slot);
        record.setEntryTime(LocalDateTime.now().minusMinutes(30));
        record.setStatus(RecordStatus.ACTIVE);

        when(recordRepository.findByTicketId("TKT-20261022-00001")).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> parkingService.processExit(req, otherCustomer))
                .isInstanceOf(SecurityException.class);
    }

    // (7) Double exit is rejected
    @Test
    void processExit_alreadyCompleted_throwsIllegalStateException() {
        ParkingExitRequest req = new ParkingExitRequest();
        req.setTicketId("TKT-20261022-00001");
        req.setPaymentMethod("CASH");

        ParkingRecord record = new ParkingRecord();
        record.setTicketId("TKT-20261022-00001");
        record.setVehicle(vehicle);
        record.setStatus(RecordStatus.COMPLETED);

        when(recordRepository.findByTicketId("TKT-20261022-00001")).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> parkingService.processExit(req, customer))
                .isInstanceOf(IllegalStateException.class);
    }

    // (8) Fee within grace period is zero
    @Test
    void processExit_withinGracePeriod_feeIsZero() {
        ParkingExitRequest req = new ParkingExitRequest();
        req.setTicketId("TKT-20261022-00002");
        req.setPaymentMethod("CARD");

        ParkingRecord record = new ParkingRecord();
        record.setId(2L);
        record.setTicketId("TKT-20261022-00002");
        record.setVehicle(vehicle);
        record.setSlot(slot);
        record.setEntryTime(LocalDateTime.now().minusMinutes(10)); // within 15 min grace
        record.setStatus(RecordStatus.ACTIVE);

        when(recordRepository.findByTicketId("TKT-20261022-00002")).thenReturn(Optional.of(record));
        when(rateScheduleRepository.findFirstByVehicleTypeAndIsActiveTrue(VehicleType.CAR)).thenReturn(Optional.of(rate));

        ParkingExitResponse resp = parkingService.processExit(req, customer);

        assertThat(resp.getFeeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // (9) Staff can create a slot
    @Test
    void createSlot_success() {
        when(slotRepository.findAll()).thenReturn(List.of());

        var slotReq = new com.vps.dto.ParkingSlotRequest();
        slotReq.setSlotNumber("C-01");
        slotReq.setSlotType(ParkingSlot.SlotType.REGULAR);
        slotReq.setFloor("2");

        when(slotRepository.save(any(ParkingSlot.class))).thenAnswer(inv -> {
            ParkingSlot s = inv.getArgument(0);
            s.setId(99L);
            return s;
        });

        var resp = parkingService.createSlot(slotReq);

        assertThat(resp.getSlotNumber()).isEqualTo("C-01");
        assertThat(resp.getStatus()).isEqualTo(SlotStatus.AVAILABLE);
    }

    // (10) Duplicate slot number is rejected
    @Test
    void createSlot_duplicateNumber_throwsIllegalArgumentException() {
        ParkingSlot existing = new ParkingSlot();
        existing.setSlotNumber("A-01");

        when(slotRepository.findAll()).thenReturn(List.of(existing));

        var slotReq = new com.vps.dto.ParkingSlotRequest();
        slotReq.setSlotNumber("a-01"); // lowercase should still conflict
        slotReq.setSlotType(ParkingSlot.SlotType.REGULAR);

        assertThatThrownBy(() -> parkingService.createSlot(slotReq))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    // (11) Slot history ownership — customer cannot view another's vehicle history
    @Test
    void getParkingHistory_otherUserVehicle_throwsSecurityException() {
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> parkingService.getParkingHistory(10L, otherCustomer))
                .isInstanceOf(SecurityException.class);
    }

    // (12) Staff can view any vehicle's history
    @Test
    void getParkingHistory_staffCanViewAny() {
        User attendant = new User();
        attendant.setId(99L);
        attendant.setRole(User.Role.PARKING_ATTENDANT);

        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(recordRepository.findByVehicleIdOrderByEntryTimeDesc(10L)).thenReturn(List.of());

        var result = parkingService.getParkingHistory(10L, attendant);
        assertThat(result).isNotNull();
    }
}
