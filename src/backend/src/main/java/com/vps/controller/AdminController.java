package com.vps.controller;

import com.vps.dto.AdminDashboardResponse;
import com.vps.dto.ApiResponse;
import com.vps.dto.ParkingHistoryResponse;
import com.vps.dto.RateScheduleRequest;
import com.vps.dto.RateScheduleResponse;
import com.vps.service.AdminService;
import com.vps.service.RateScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminController {

    private final AdminService adminService;
    private final RateScheduleService rateScheduleService;

    // GET /api/v1/admin/dashboard — VPS-F-013
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dashboard data retrieved",
                        adminService.getDashboard()));
    }

    // GET /api/v1/admin/rates
    @GetMapping("/rates")
    public ResponseEntity<ApiResponse<List<RateScheduleResponse>>> getRateSchedules() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Rate schedules retrieved",
                        rateScheduleService.getAll()));
    }

    // POST /api/v1/admin/rates
    @PostMapping("/rates")
    public ResponseEntity<ApiResponse<RateScheduleResponse>> createRateSchedule(
            @Valid @RequestBody RateScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Rate schedule created",
                        rateScheduleService.create(request)));
    }

    // PUT /api/v1/admin/rates/{id}
    @PutMapping("/rates/{id}")
    public ResponseEntity<ApiResponse<RateScheduleResponse>> updateRateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody RateScheduleRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Rate schedule updated",
                        rateScheduleService.update(id, request)));
    }

    // PATCH /api/v1/admin/rates/{id}/deactivate
    @PatchMapping("/rates/{id}/deactivate")
    public ResponseEntity<ApiResponse<RateScheduleResponse>> deactivateRateSchedule(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Rate schedule deactivated",
                        rateScheduleService.deactivate(id)));
    }

    // GET /api/v1/admin/reports?from=...&to=... — VPS-F-015
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ParkingHistoryResponse>>> getReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Report generated",
                        adminService.getReport(from, to)));
    }
}
