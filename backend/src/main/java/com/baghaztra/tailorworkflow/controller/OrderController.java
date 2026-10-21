package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.entity.Order;
import com.baghaztra.tailorworkflow.repository.OrderRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orders;

    public OrderController(OrderRepository orders) {
        this.orders = orders;
    }

    @GetMapping
    public List<OrderResponse> list(@RequestParam(required = false) Order.Status status) {
        List<Order> result = status == null ? orders.findAll() : orders.findByStatus(status);
        return result.stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return OrderResponse.from(find(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody SaveOrderRequest request) {
        Order order = new Order(request.customerName());
        order.setTotalPrice(request.totalPrice());
        return OrderResponse.from(orders.save(order));
    }

    @PutMapping("/{id}")
    public OrderResponse update(@PathVariable Long id, @Valid @RequestBody SaveOrderRequest request) {
        Order order = find(id);
        order.setCustomerName(request.customerName());
        order.setTotalPrice(request.totalPrice());
        return OrderResponse.from(orders.save(order));
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        Order order = find(id);
        order.setStatus(request.status());
        order.setStage(request.stage());
        return OrderResponse.from(orders.save(order));
    }

    private Order find(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
    }

    public record SaveOrderRequest(
            @NotBlank @Size(max = 100) String customerName,
            @DecimalMin("0.00") BigDecimal totalPrice) {
    }

    public record UpdateStatusRequest(
            @NotNull Order.Status status,
            Order.Stage stage) {
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
