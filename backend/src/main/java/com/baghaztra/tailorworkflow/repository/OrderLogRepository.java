package com.baghaztra.tailorworkflow.repository;

import com.baghaztra.tailorworkflow.entity.Order;
import com.baghaztra.tailorworkflow.entity.OrderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderLogRepository extends JpaRepository<OrderLog, Long> {

    List<OrderLog> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    Optional<OrderLog> findTopByOrderIdAndStageOrderByCreatedAtDesc(Long orderId, Order.Stage stage);

    Optional<OrderLog> findTopByOrderIdAndActionOrderByCreatedAtDesc(Long orderId, OrderLog.Action action);
}
