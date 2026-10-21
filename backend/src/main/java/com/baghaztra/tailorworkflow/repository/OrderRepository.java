package com.baghaztra.tailorworkflow.repository;

import com.baghaztra.tailorworkflow.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(Order.Status status);
}
