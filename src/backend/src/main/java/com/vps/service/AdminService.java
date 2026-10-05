package com.vps.service;

import com.vps.dto.AdminDashboardResponse;
import com.vps.dto.ParkingHistoryResponse;
import com.vps.entity.ParkingSlot.SlotStatus;
import com.vps.entity.ParkingRecord.RecordStatus;
import com.vps.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ParkingSlotRepository slotRepository;
    private final ParkingRecordRepository recordRepository;
    private final UserRepository userRepository;
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

    // VPS-F-015: Report — history with optional date range
    public List<ParkingHistoryResponse> getReport(LocalDateTime from, LocalDateTime to) {
        if (from == null) from = LocalDateTime.now().minusDays(30);
        if (to == null) to = LocalDateTime.now();
        return recordRepository.findByEntryTimeBetweenOrderByEntryTimeDesc(from, to)
                .stream()
                .map(rec -> {
                    // reuse history mapper from ParkingService
                    return parkingService.getAllParkingHistory().stream()
                            .filter(h -> h.getTicketId().equals(rec.getTicketId()))
                            .findFirst().orElse(null);
                })
                .filter(h -> h != null)
                .toList();
    }
}
