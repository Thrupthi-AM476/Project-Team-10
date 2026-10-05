package com.vps.controller;

import com.vps.dto.AdminDashboardResponse;
import com.vps.dto.ApiResponse;
import com.vps.dto.ParkingHistoryResponse;
import com.vps.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

    // GET /api/v1/admin/dashboard — VPS-F-013
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard data retrieved", adminService.getDashboard()));
    }

    // GET /api/v1/admin/reports?from=...&to=... — VPS-F-015
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ParkingHistoryResponse>>> getReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(
                ApiResponse.success("Report generated", adminService.getReport(from, to)));
    }
}
