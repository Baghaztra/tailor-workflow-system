package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.entity.Order;
import com.baghaztra.tailorworkflow.entity.Payment;
import com.baghaztra.tailorworkflow.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping
    public List<OrderResponse> list(@RequestParam(required = false) Order.Status status) {
        return orders.list(status).stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return OrderResponse.from(orders.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody SaveOrderRequest request) {
        return OrderResponse.from(orders.create(request.customerName(), request.totalPrice()));
    }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable Long id, @Valid @RequestBody SaveOrderRequest request) {
        return OrderResponse.from(orders.update(id, request.customerName(), request.totalPrice()));
    }

    @PostMapping("/{id}/assign")
    public OrderResponse assign(@PathVariable Long id, @Valid @RequestBody AssignWorkerRequest request) {
        return OrderResponse.from(orders.assign(id, request.workerId()));
    }

    @PostMapping("/{id}/start")
    public OrderResponse start(@PathVariable Long id) {
        return OrderResponse.from(orders.start(id));
    }

    @PostMapping("/{id}/complete")
    public OrderResponse complete(@PathVariable Long id) {
        return OrderResponse.from(orders.complete(id));
    }

    @PostMapping("/{id}/pickup")
    public OrderResponse pickup(@PathVariable Long id) {
        return OrderResponse.from(orders.pickup(id));
    }

    @PostMapping("/{id}/payment")
    public OrderResponse pay(@PathVariable Long id, @Valid @RequestBody PayRequest request) {
        return OrderResponse.from(orders.pay(id, request.amount(), request.method()));
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return OrderResponse.from(orders.cancel(id));
    }

    public record SaveOrderRequest(
            @NotBlank @Size(max = 100) String customerName,
            @DecimalMin("0.00") BigDecimal totalPrice) {
    }

    public record AssignWorkerRequest(@NotNull Long workerId) {
    }

    public record PayRequest(
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotNull Payment.Method method) {
    }

    public record OrderResponse(
            Long id,
            String customerName,
            Order.Status status,
            Order.Stage stage,
            BigDecimal totalPrice,
            Instant createdAt,
            Instant updatedAt) {

        static OrderResponse from(Order order) {
            return new OrderResponse(
                    order.getId(),
                    order.getCustomerName(),
                    order.getStatus(),
                    order.getStage(),
                    order.getTotalPrice(),
                    order.getCreatedAt(),
                    order.getUpdatedAt());
        }
    }
}
