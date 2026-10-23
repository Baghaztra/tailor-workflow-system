package com.baghaztra.tailorworkflow;

import com.baghaztra.tailorworkflow.entity.Order;
import com.baghaztra.tailorworkflow.entity.OrderLog;
import com.baghaztra.tailorworkflow.entity.Payment;
import com.baghaztra.tailorworkflow.entity.Worker;
import com.baghaztra.tailorworkflow.repository.OrderLogRepository;
import com.baghaztra.tailorworkflow.repository.PaymentRepository;
import com.baghaztra.tailorworkflow.repository.WorkerRepository;
import com.baghaztra.tailorworkflow.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderWorkflowIntegrationTest extends IntegrationTest {

    @Autowired
    private OrderService orders;

    @Autowired
    private WorkerRepository workers;

    @Autowired
    private OrderLogRepository logs;

    @Autowired
    private PaymentRepository payments;

    @Test
    void completesFullWorkflowAndWritesTimeline() {
        Worker cutting = worker("Cutter", Worker.Specialization.CUTTING);
        Worker sewing = worker("Sewer", Worker.Specialization.SEWING);
        Worker finishing = worker("Finisher", Worker.Specialization.FINISHING);
        Order order = orders.create("Ayu", new BigDecimal("250000.00"));

        orders.assign(order.getId(), cutting.getId());
        orders.start(order.getId());
        orders.complete(order.getId());
        orders.assign(order.getId(), sewing.getId());
        orders.start(order.getId());
        orders.complete(order.getId());
        orders.assign(order.getId(), finishing.getId());
        orders.start(order.getId());
        orders.complete(order.getId());
        orders.pickup(order.getId());
        Order completed = orders.pay(order.getId(), new BigDecimal("250000.00"), Payment.Method.CASH);

        assertEquals(Order.Status.COMPLETED, completed.getStatus());
        assertNull(completed.getStage());
        assertTrue(payments.existsByOrderId(order.getId()));
        assertEquals(
                List.of(
                        OrderLog.Action.CREATED,
                        OrderLog.Action.ASSIGNED,
                        OrderLog.Action.STARTED,
                        OrderLog.Action.COMPLETED,
                        OrderLog.Action.ASSIGNED,
                        OrderLog.Action.STARTED,
                        OrderLog.Action.COMPLETED,
                        OrderLog.Action.ASSIGNED,
                        OrderLog.Action.STARTED,
                        OrderLog.Action.COMPLETED,
                        OrderLog.Action.READY_FOR_PICKUP,
                        OrderLog.Action.PICKED_UP,
                        OrderLog.Action.PAYMENT_RECORDED),
                actions(order.getId()));
    }

    @Test
    void rejectsStartingTheSameStageTwiceWithoutAddingALog() {
        Order order = orders.create("Ayu", null);
        Worker cutting = worker("Cutter", Worker.Specialization.CUTTING);
        orders.assign(order.getId(), cutting.getId());
        orders.start(order.getId());
        long before = logs.count();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> orders.start(order.getId()));

        assertEquals(HttpStatus.CONFLICT.value(), error.getStatusCode().value());
        assertEquals(before, logs.count());
    }

    @Test
    void rejectsWorkerForTheWrongStage() {
        Order order = orders.create("Ayu", null);
        Worker sewing = worker("Sewer", Worker.Specialization.SEWING);
        long before = logs.count();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> orders.assign(order.getId(), sewing.getId()));

        assertEquals(HttpStatus.CONFLICT.value(), error.getStatusCode().value());
        assertEquals(before, logs.count());
        assertEquals(Order.Status.CREATED, orders.get(order.getId()).getStatus());
    }

    @Test
    void rejectsASecondPayment() {
        Order order = runToPickedUp();
        orders.pay(order.getId(), new BigDecimal("250000.00"), Payment.Method.TRANSFER);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> orders.pay(order.getId(), new BigDecimal("250000.00"), Payment.Method.CASH));

        assertEquals(HttpStatus.CONFLICT.value(), error.getStatusCode().value());
        assertEquals(1, payments.count());
    }

    @Test
    void cancellingAnOrderWritesACancelledLog() {
        Order order = orders.create("Ayu", null);

        Order cancelled = orders.cancel(order.getId());

        assertEquals(Order.Status.CANCELLED, cancelled.getStatus());
        assertTrue(actions(order.getId()).contains(OrderLog.Action.CANCELLED));
    }

    private Order runToPickedUp() {
        Worker cutting = worker("Cutter", Worker.Specialization.CUTTING);
        Worker sewing = worker("Sewer", Worker.Specialization.SEWING);
        Worker finishing = worker("Finisher", Worker.Specialization.FINISHING);
        Order order = orders.create("Ayu", new BigDecimal("250000.00"));

        orders.assign(order.getId(), cutting.getId());
        orders.start(order.getId());
        orders.complete(order.getId());
        orders.assign(order.getId(), sewing.getId());
        orders.start(order.getId());
        orders.complete(order.getId());
        orders.assign(order.getId(), finishing.getId());
        orders.start(order.getId());
        orders.complete(order.getId());
        return orders.pickup(order.getId());
    }

    private Worker worker(String name, Worker.Specialization specialization) {
        return workers.save(new Worker(name, specialization));
    }

    private List<OrderLog.Action> actions(Long orderId) {
        return orders.listLogs(orderId).stream().map(OrderLog::getAction).toList();
    }
}
