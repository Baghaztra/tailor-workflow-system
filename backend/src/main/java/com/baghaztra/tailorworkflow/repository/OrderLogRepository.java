package com.baghaztra.tailorworkflow.repository;

import com.baghaztra.tailorworkflow.entity.OrderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderLogRepository extends JpaRepository<OrderLog, Long> {

    List<OrderLog> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}
