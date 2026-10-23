package com.baghaztra.tailorworkflow.service;

import com.baghaztra.tailorworkflow.entity.Order;
import com.baghaztra.tailorworkflow.repository.OrderRepository;
import com.baghaztra.tailorworkflow.repository.WorkerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final OrderRepository orders;
    private final WorkerRepository workers;
    private final FinanceService finance;

    public DashboardService(OrderRepository orders, WorkerRepository workers, FinanceService finance) {
        this.orders = orders;
        this.workers = workers;
        this.finance = finance;
    }

    public record Dashboard(
            long activeOrders,
            long pendingPickups,
            long activeWorkers,
            long totalOrders,
            BigDecimal monthRevenue,
            BigDecimal totalRevenue) {
    }

    @Transactional(readOnly = true)
    public Dashboard get() {
        FinanceService.Summary summary = finance.summary();

        return new Dashboard(
                orders.countByStatusIn(List.of(
                        Order.Status.CREATED,
                        Order.Status.IN_PROGRESS,
                        Order.Status.READY_FOR_PICKUP)),
                orders.countByStatus(Order.Status.READY_FOR_PICKUP),
                workers.countByActiveTrue(),
                orders.count(),
                summary.monthRevenue(),
                summary.totalRevenue());
    }
}
