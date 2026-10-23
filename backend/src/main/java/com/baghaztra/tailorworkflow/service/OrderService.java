package com.baghaztra.tailorworkflow.service;

import com.baghaztra.tailorworkflow.entity.Order;
import com.baghaztra.tailorworkflow.entity.OrderLog;
import com.baghaztra.tailorworkflow.entity.Payment;
import com.baghaztra.tailorworkflow.entity.Worker;
import com.baghaztra.tailorworkflow.repository.OrderLogRepository;
import com.baghaztra.tailorworkflow.repository.OrderRepository;
import com.baghaztra.tailorworkflow.repository.PaymentRepository;
import com.baghaztra.tailorworkflow.repository.WorkerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final OrderLogRepository logs;
    private final WorkerRepository workers;
    private final PaymentRepository payments;

    public OrderService(OrderRepository orders, OrderLogRepository logs, WorkerRepository workers,
            PaymentRepository payments) {
        this.orders = orders;
        this.logs = logs;
        this.workers = workers;
        this.payments = payments;
    }

    @Transactional(readOnly = true)
    public List<Order> list(Order.Status status) {
        return status == null ? orders.findAll() : orders.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Order get(Long id) {
        return find(id);
    }

    @Transactional
    public Order create(String customerName, BigDecimal totalPrice) {
        Order order = new Order(customerName);
        order.setTotalPrice(totalPrice);
        orders.save(order);
        log(order, OrderLog.Action.CREATED, null, null, null);
        return order;
    }

    @Transactional
    public Order update(Long id, String customerName, BigDecimal totalPrice) {
        Order order = find(id);
        requireStatus(order, Order.Status.CREATED, Order.Status.IN_PROGRESS, Order.Status.READY_FOR_PICKUP);
        order.setCustomerName(customerName);
        order.setTotalPrice(totalPrice);
        return orders.save(order);
    }

    @Transactional
    public Order assign(Long id, Long workerId) {
        Order order = find(id);
        requireStatus(order, Order.Status.CREATED, Order.Status.IN_PROGRESS);
        if (order.getStage() != null) {
            throw conflict(order, "stage " + order.getStage() + " is still in progress");
        }

        Worker worker = workers.findById(workerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker " + workerId + " not found"));
        if (!worker.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Worker " + workerId + " is not active");
        }

        Order.Stage stage = Order.Stage.valueOf(worker.getSpecialization().name());
        order.setStage(stage);
        order.setStatus(Order.Status.IN_PROGRESS);
        orders.save(order);
        log(order, OrderLog.Action.ASSIGNED, stage, worker, "Assigned to " + worker.getName());
        return order;
    }

    @Transactional
    public Order start(Long id) {
        Order order = find(id);
        requireStatus(order, Order.Status.IN_PROGRESS);
        if (order.getStage() == null) {
            throw conflict(order, "no stage has been assigned");
        }
        log(order, OrderLog.Action.STARTED, order.getStage(), null, null);
        return order;
    }

    @Transactional
    public Order complete(Long id) {
        Order order = find(id);
        requireStatus(order, Order.Status.IN_PROGRESS);
        Order.Stage stage = order.getStage();
        if (stage == null) {
            throw conflict(order, "no stage has been assigned");
        }

        log(order, OrderLog.Action.COMPLETED, stage, null, null);
        order.setStage(null);

        if (stage == Order.Stage.FINISHING) {
            order.setStatus(Order.Status.READY_FOR_PICKUP);
            log(order, OrderLog.Action.READY_FOR_PICKUP, stage, null, null);
        }

        orders.save(order);
        return order;
    }

    @Transactional
    public Order pickup(Long id) {
        Order order = find(id);
        requireStatus(order, Order.Status.READY_FOR_PICKUP);
        order.setStatus(Order.Status.PICKED_UP);
        orders.save(order);
        log(order, OrderLog.Action.PICKED_UP, null, null, null);
        return order;
    }

    @Transactional
    public Order pay(Long id, BigDecimal amount, Payment.Method method) {
        Order order = find(id);
        requireStatus(order, Order.Status.PICKED_UP);
        if (payments.existsByOrderId(id)) {
            throw conflict(order, "payment has already been recorded");
        }

        payments.save(new Payment(order, amount, method));
        order.setStatus(Order.Status.COMPLETED);
        orders.save(order);
        log(order, OrderLog.Action.PAYMENT_RECORDED, null, null, null);
        return order;
    }

    @Transactional
    public Order cancel(Long id) {
        Order order = find(id);
        if (order.getStatus() == Order.Status.COMPLETED || order.getStatus() == Order.Status.CANCELLED) {
            throw conflict(order, "it cannot be cancelled");
        }
        order.setStatus(Order.Status.CANCELLED);
        order.setStage(null);
        orders.save(order);
        log(order, OrderLog.Action.CANCELLED, null, null, null);
        return order;
    }

    @Transactional(readOnly = true)
    public List<OrderLog> listLogs(Long orderId) {
        find(orderId);
        return logs.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    private void log(Order order, OrderLog.Action action, Order.Stage stage, Worker worker, String note) {
        OrderLog entry = new OrderLog(order, action);
        entry.setStage(stage);
        entry.setWorker(worker);
        entry.setNote(note);
        logs.save(entry);
    }

    private Order find(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order " + id + " not found"));
    }

    private void requireStatus(Order order, Order.Status... allowed) {
        if (Arrays.stream(allowed).noneMatch(status -> status == order.getStatus())) {
            throw conflict(order, "status " + order.getStatus() + " does not allow this action");
        }
    }

    private ResponseStatusException conflict(Order order, String reason) {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Order " + order.getId() + ": " + reason);
    }
}
