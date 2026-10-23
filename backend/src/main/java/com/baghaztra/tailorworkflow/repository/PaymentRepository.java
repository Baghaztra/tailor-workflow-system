package com.baghaztra.tailorworkflow.repository;

import com.baghaztra.tailorworkflow.entity.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByOrderId(Long orderId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p")
    BigDecimal sumAmount();

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.paidAt >= :since")
    BigDecimal sumAmountSince(@Param("since") Instant since);

    @EntityGraph(attributePaths = "order")
    List<Payment> findAllByOrderByPaidAtDesc();

    @EntityGraph(attributePaths = "order")
    List<Payment> findByPaidAtGreaterThanEqualOrderByPaidAtDesc(Instant from);

    @EntityGraph(attributePaths = "order")
    List<Payment> findByPaidAtLessThanOrderByPaidAtDesc(Instant to);

    @EntityGraph(attributePaths = "order")
    List<Payment> findByPaidAtGreaterThanEqualAndPaidAtLessThanOrderByPaidAtDesc(Instant from, Instant to);
}
