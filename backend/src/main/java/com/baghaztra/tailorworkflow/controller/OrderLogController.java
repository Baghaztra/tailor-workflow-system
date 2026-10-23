package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.entity.Order;
import com.baghaztra.tailorworkflow.entity.OrderLog;
import com.baghaztra.tailorworkflow.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/logs")
public class OrderLogController {

    private final OrderService orders;

    public OrderLogController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    public List<OrderLogResponse> list(@PathVariable Long orderId) {
        return orders.listLogs(orderId).stream().map(OrderLogResponse::from).toList();
    }

    public record OrderLogResponse(
            Long id,
            Long orderId,
            Long workerId,
            Order.Stage stage,
            OrderLog.Action action,
            String note,
            Instant createdAt) {

        static OrderLogResponse from(OrderLog log) {
            return new OrderLogResponse(
                    log.getId(),
                    log.getOrder().getId(),
                    log.getWorker() == null ? null : log.getWorker().getId(),
                    log.getStage(),
                    log.getAction(),
                    log.getNote(),
                    log.getCreatedAt());
        }
    }
}
