package com.seaandtea.controller;

import com.seaandtea.dto.AdminStatisticsResponse;
import com.seaandtea.service.AdminStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Statistics", description = "Counts for the admin dashboard")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    @GetMapping
    @Operation(summary = "Dashboard counts", description = "User, verified-guide, pending-guide, and booking counts. Does not load full records.")
    public AdminStatisticsResponse getStatistics() {
        return adminStatisticsService.getStatistics();
    }
}
