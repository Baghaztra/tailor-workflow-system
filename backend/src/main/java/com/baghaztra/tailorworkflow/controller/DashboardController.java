package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping
    public DashboardResponse get() {
        return DashboardResponse.from(dashboard.get());
    }

    public record DashboardResponse(
            long activeOrders,
            long pendingPickups,
            long activeWorkers,
            long totalOrders,
            BigDecimal monthRevenue,
            BigDecimal totalRevenue) {

        static DashboardResponse from(DashboardService.Dashboard dashboard) {
            return new DashboardResponse(
                    dashboard.activeOrders(),
                    dashboard.pendingPickups(),
                    dashboard.activeWorkers(),
                    dashboard.totalOrders(),
                    dashboard.monthRevenue(),
                    dashboard.totalRevenue());
        }
    }
}
