package com.vps.service;

import com.vps.dto.AdminDashboardResponse;
import com.vps.dto.ParkingHistoryResponse;
import com.vps.dto.RateScheduleRequest;
import com.vps.dto.RateScheduleResponse;
import com.vps.entity.ParkingSlot.SlotStatus;
import com.vps.entity.ParkingRecord.RecordStatus;
import com.vps.entity.RateSchedule;
import com.vps.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ParkingSlotRepository slotRepository;
    private final ParkingRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final RateScheduleRepository rateScheduleRepository;
    private final ParkingService parkingService;

    // VPS-F-013: Admin dashboard with real data
    public AdminDashboardResponse getDashboard() {
        AdminDashboardResponse dash = new AdminDashboardResponse();

        dash.setTotalSlots(slotRepository.count());
        dash.setAvailableSlots(slotRepository.countByStatus(SlotStatus.AVAILABLE));
        dash.setOccupiedSlots(slotRepository.countByStatus(SlotStatus.OCCUPIED));
        dash.setReservedSlots(slotRepository.countByStatus(SlotStatus.RESERVED));
        dash.setOutOfServiceSlots(slotRepository.countByStatus(SlotStatus.OUT_OF_SERVICE));
        dash.setActiveVehicles(recordRepository.countActiveRecords());
        dash.setTotalUsersRegistered(userRepository.count());
        dash.setTotalCompletedTransactions(
                recordRepository.findByStatusOrderByEntryTimeDesc(RecordStatus.COMPLETED).size());

        // Revenue today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal todayRevenue = recordRepository.sumRevenueByDateRange(startOfDay, now);
        dash.setRevenueToday(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        // Revenue this month
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        BigDecimal monthRevenue = recordRepository.sumRevenueByDateRange(startOfMonth, now);
        dash.setRevenueThisMonth(monthRevenue != null ? monthRevenue : BigDecimal.ZERO);

        return dash;
    }

    // VPS-F-015: Report — history with optional date range (efficient query)
    public List<ParkingHistoryResponse> getReport(LocalDateTime from, LocalDateTime to) {
        if (from == null) from = LocalDateTime.now().minusDays(30);
        if (to == null) to = LocalDateTime.now();
        return recordRepository.findByEntryTimeBetweenOrderByEntryTimeDesc(from, to)
                .stream()
                .map(parkingService::toHistoryResponsePublic)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // VPS-F-014: Rate schedule configuration
    // ---------------------------------------------------------------
    public List<RateScheduleResponse> getAllRates() {
        return rateScheduleRepository.findAll().stream()
                .map(this::toRateResponse).collect(Collectors.toList());
    }

    public List<RateScheduleResponse> getActiveRates() {
        return rateScheduleRepository.findByIsActiveTrue().stream()
                .map(this::toRateResponse).collect(Collectors.toList());
    }

    @Transactional
    public RateScheduleResponse createRate(RateScheduleRequest request) {
        RateSchedule rate = new RateSchedule();
        rate.setName(request.getName());
        rate.setVehicleType(request.getVehicleType());
        rate.setRatePerHour(request.getRatePerHour());
        rate.setGracePeriodMinutes(request.getGracePeriodMinutes());
        rate.setActive(request.isActive());
        rateScheduleRepository.save(rate);
        return toRateResponse(rate);
    }

    @Transactional
    public RateScheduleResponse updateRate(Long rateId, RateScheduleRequest request) {
        RateSchedule rate = rateScheduleRepository.findById(rateId)
                .orElseThrow(() -> new IllegalArgumentException("Rate schedule not found: " + rateId));

        rate.setName(request.getName());
        rate.setVehicleType(request.getVehicleType());
        rate.setRatePerHour(request.getRatePerHour());
        rate.setGracePeriodMinutes(request.getGracePeriodMinutes());
        rate.setActive(request.isActive());
        rateScheduleRepository.save(rate);
        return toRateResponse(rate);
    }

    private RateScheduleResponse toRateResponse(RateSchedule r) {
        RateScheduleResponse resp = new RateScheduleResponse();
        resp.setId(r.getId());
        resp.setName(r.getName());
        resp.setVehicleType(r.getVehicleType());
        resp.setRatePerHour(r.getRatePerHour());
        resp.setGracePeriodMinutes(r.getGracePeriodMinutes());
        resp.setActive(r.isActive());
        resp.setCreatedAt(r.getCreatedAt());
        return resp;
    }
}
