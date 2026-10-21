package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.entity.Order;
import com.baghaztra.tailorworkflow.entity.OrderLog;
import com.baghaztra.tailorworkflow.entity.Worker;
import com.baghaztra.tailorworkflow.repository.OrderLogRepository;
import com.baghaztra.tailorworkflow.repository.OrderRepository;
import com.baghaztra.tailorworkflow.repository.WorkerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/logs")
public class OrderLogController {

    private final OrderLogRepository logs;
    private final OrderRepository orders;
    private final WorkerRepository workers;

    public OrderLogController(OrderLogRepository logs, OrderRepository orders, WorkerRepository workers) {
        this.logs = logs;
        this.orders = orders;
        this.workers = workers;
    }

    @GetMapping
    public List<OrderLogResponse> list(@PathVariable Long orderId) {
        findOrder(orderId);
        return logs.findByOrderIdOrderByCreatedAtAsc(orderId).stream()
                .map(OrderLogResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderLogResponse create(@PathVariable Long orderId, @Valid @RequestBody CreateOrderLogRequest request) {
        Order order = findOrder(orderId);
        OrderLog log = new OrderLog(order, request.action());
        log.setStage(request.stage());
        log.setNote(request.note());
        if (request.workerId() != null) {
            log.setWorker(findWorker(request.workerId()));
        }
        return OrderLogResponse.from(logs.save(log));
    }

    private Order findOrder(Long orderId) {
        return orders.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + orderId + " not found"));
    }

    private Worker findWorker(Long workerId) {
        return workers.findById(workerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker " + workerId + " not found"));
    }

    public record CreateOrderLogRequest(
            @NotNull OrderLog.Action action,
            Order.Stage stage,
            Long workerId,
            @Size(max = 500) String note) {
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
