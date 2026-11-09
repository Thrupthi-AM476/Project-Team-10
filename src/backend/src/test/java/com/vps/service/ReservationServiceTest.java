package com.vps.service;

import com.vps.dto.ReservationRequest;
import com.vps.dto.ReservationResponse;
import com.vps.entity.*;
import com.vps.entity.ParkingSlot.SlotStatus;
import com.vps.entity.ParkingSlot.SlotType;
import com.vps.entity.Reservation.ReservationStatus;
import com.vps.repository.ParkingSlotRepository;
import com.vps.repository.ReservationRepository;
import com.vps.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ParkingSlotRepository slotRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ReservationService reservationService;

    private User customer;
    private User admin;
    private Vehicle vehicle;
    private ParkingSlot slot;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        customer.setFullName("Test Customer");
        customer.setRole(User.Role.CUSTOMER);

        admin = new User();
        admin.setId(99L);
        admin.setFullName("Test Admin");
        admin.setRole(User.Role.ADMINISTRATOR);

        vehicle = new Vehicle();
        vehicle.setId(10L);
        vehicle.setLicensePlate("KA-01-AB-1234");
        vehicle.setUser(customer);

        slot = new ParkingSlot();
        slot.setId(100L);
        slot.setSlotNumber("A-01");
        slot.setSlotType(SlotType.REGULAR);
        slot.setStatus(SlotStatus.AVAILABLE);
    }

    @Test
    void createReservation_Success_WithPreferredType() {
        ReservationRequest request = new ReservationRequest();
        request.setVehicleId(10L);
        request.setSlotType("REGULAR");
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(3));

        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(slotRepository.findByStatusAndSlotType(SlotStatus.AVAILABLE, SlotType.REGULAR))
                .thenReturn(List.of(slot));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation r = i.getArgument(0);
            r.setId(500L);
            return r;
        });

        ReservationResponse response = reservationService.createReservation(request, customer);

        assertNotNull(response);
        assertEquals(500L, response.getId());
        assertEquals("A-01", response.getSlotNumber());
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
        verify(auditLogService).log(any(), any(), any(), any(), any());
    }

    @Test
    void createReservation_Success_WithoutPreferredType() {
        ReservationRequest request = new ReservationRequest();
        request.setVehicleId(10L);
        request.setSlotType(null); // null slotType branch
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(slotRepository.findByStatus(SlotStatus.AVAILABLE)).thenReturn(List.of(slot));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation r = i.getArgument(0);
            r.setId(501L);
            return r;
        });

        ReservationResponse response = reservationService.createReservation(request, customer);

        assertNotNull(response);
        assertEquals(501L, response.getId());
    }

    @Test
    void createReservation_EndTimeNotAfterStartTime_ThrowsException() {
        ReservationRequest request = new ReservationRequest();
        request.setStartTime(LocalDateTime.now().plusHours(2));
        request.setEndTime(LocalDateTime.now().plusHours(1)); // Invalid window

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(request, customer));
    }

    @Test
    void createReservation_VehicleNotFound_ThrowsException() {
        ReservationRequest request = new ReservationRequest();
        request.setVehicleId(999L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reservationService.createReservation(request, customer));
    }

    @Test
    void createReservation_VehicleNotOwnedByCustomer_ThrowsException() {
        ReservationRequest request = new ReservationRequest();
        request.setVehicleId(10L);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        User otherUser = new User();
        otherUser.setId(2L);
        vehicle.setUser(otherUser);

        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));

        assertThrows(SecurityException.class, () -> reservationService.createReservation(request, customer));
    }

    @Test
    void createReservation_NoAvailableSlots_ThrowsException() {
        ReservationRequest request = new ReservationRequest();
        request.setVehicleId(10L);
        request.setSlotType("REGULAR");
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(slotRepository.findByStatusAndSlotType(SlotStatus.AVAILABLE, SlotType.REGULAR))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> reservationService.createReservation(request, customer));
    }

    @Test
    void createReservation_SlotReservedInWindow_ThrowsException() {
        ReservationRequest request = new ReservationRequest();
        request.setVehicleId(10L);
        request.setSlotType("REGULAR");
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);
        request.setStartTime(start);
        request.setEndTime(end);

        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(slotRepository.findByStatusAndSlotType(SlotStatus.AVAILABLE, SlotType.REGULAR))
                .thenReturn(List.of(slot));
        when(reservationRepository.isSlotReservedInWindow(slot.getId(), start, end)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> reservationService.createReservation(request, customer));
    }

    @Test
    void getUserReservations_And_GetAllReservations_ReturnResponses() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setUser(customer);
        r.setVehicle(vehicle);
        r.setSlot(slot);
        r.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findByUserIdOrderByStartTimeDesc(1L)).thenReturn(List.of(r));
        when(reservationRepository.findAll()).thenReturn(List.of(r));

        List<ReservationResponse> userList = reservationService.getUserReservations(1L);
        List<ReservationResponse> allList = reservationService.getAllReservations();

        assertEquals(1, userList.size());
        assertEquals(1, allList.size());
    }

    @Test
    void cancelReservation_AsOwner_Success() {
        Reservation r = new Reservation();
        r.setId(100L);
        r.setUser(customer);
        r.setVehicle(vehicle);
        r.setSlot(slot);
        r.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = reservationService.cancelReservation(100L, customer);

        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
        verify(auditLogService).log(any(), any(), any(), any(), any());
    }

    @Test
    void cancelReservation_AsAdmin_OverridesOwnership() {
        Reservation r = new Reservation();
        r.setId(100L);
        r.setUser(customer);
        r.setVehicle(vehicle);
        r.setSlot(slot);
        r.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(r));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse response = reservationService.cancelReservation(100L, admin);

        assertEquals(ReservationStatus.CANCELLED, response.getStatus());
    }

    @Test
    void cancelReservation_NotFound_ThrowsException() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reservationService.cancelReservation(999L, customer));
    }

    @Test
    void cancelReservation_UnauthorizedUser_ThrowsException() {
        Reservation r = new Reservation();
        r.setId(100L);
        r.setUser(customer);
        r.setStatus(ReservationStatus.CONFIRMED);

        User stranger = new User();
        stranger.setId(55L);
        stranger.setRole(User.Role.CUSTOMER);

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(r));

        assertThrows(SecurityException.class, () -> reservationService.cancelReservation(100L, stranger));
    }

    @Test
    void cancelReservation_AlreadyCancelled_ThrowsException() {
        Reservation r = new Reservation();
        r.setId(100L);
        r.setUser(customer);
        r.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(r));

        assertThrows(IllegalStateException.class, () -> reservationService.cancelReservation(100L, customer));
    }

    @Test
    void cancelReservation_AlreadyCompleted_ThrowsException() {
        Reservation r = new Reservation();
        r.setId(100L);
        r.setUser(customer);
        r.setStatus(ReservationStatus.COMPLETED);

        when(reservationRepository.findById(100L)).thenReturn(Optional.of(r));

        assertThrows(IllegalStateException.class, () -> reservationService.cancelReservation(100L, customer));
    }

    @Test
    void expireOverdueReservations_ProcessesExpiredList() {
        Reservation r = new Reservation();
        r.setId(200L);
        r.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findExpiredReservations(any(LocalDateTime.class)))
                .thenReturn(List.of(r));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        reservationService.expireOverdueReservations();

        assertEquals(ReservationStatus.EXPIRED, r.getStatus());
        verify(reservationRepository).save(r);
        verify(auditLogService).log(isNull(), eq("RESERVATION_EXPIRED"), eq("RESERVATION"), eq(200L), anyString());
    }

    @Test
    void expireOverdueReservations_EmptyList_DoesNothing() {
        when(reservationRepository.findExpiredReservations(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        reservationService.expireOverdueReservations();

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_WithBlankAndInvalidSlotTypes() {
        ReservationRequest blankReq = new ReservationRequest();
        blankReq.setVehicleId(10L);
        blankReq.setSlotType("   ");
        blankReq.setStartTime(LocalDateTime.now().plusHours(1));
        blankReq.setEndTime(LocalDateTime.now().plusHours(2));

        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(vehicle));
        when(slotRepository.findByStatus(SlotStatus.AVAILABLE)).thenReturn(List.of(slot));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        ReservationResponse resp1 = reservationService.createReservation(blankReq, customer);
        assertNotNull(resp1);

        ReservationRequest invalidReq = new ReservationRequest();
        invalidReq.setVehicleId(10L);
        invalidReq.setSlotType("NON_EXISTENT_TYPE");
        invalidReq.setStartTime(LocalDateTime.now().plusHours(1));
        invalidReq.setEndTime(LocalDateTime.now().plusHours(2));

        ReservationResponse resp2 = reservationService.createReservation(invalidReq, customer);
        assertNotNull(resp2);
    }
}

