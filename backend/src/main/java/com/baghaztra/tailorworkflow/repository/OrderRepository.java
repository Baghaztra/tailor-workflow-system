package com.baghaztra.tailorworkflow.repository;

import com.baghaztra.tailorworkflow.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    long countByStatusIn(Collection<Order.Status> statuses);

    long countByStatus(Order.Status status);
}
