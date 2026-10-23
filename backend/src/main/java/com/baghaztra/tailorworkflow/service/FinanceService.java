package com.baghaztra.tailorworkflow.service;

import com.baghaztra.tailorworkflow.entity.Payment;
import com.baghaztra.tailorworkflow.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class FinanceService {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final PaymentRepository payments;

    public FinanceService(PaymentRepository payments) {
        this.payments = payments;
    }

    public record Summary(
            BigDecimal totalRevenue,
            BigDecimal monthRevenue,
            BigDecimal todayRevenue,
            long paymentCount) {
    }

    @Transactional(readOnly = true)
    public Summary summary() {
        LocalDate today = LocalDate.now(ZONE);
        Instant startOfToday = today.atStartOfDay(ZONE).toInstant();
        Instant startOfMonth = today.withDayOfMonth(1).atStartOfDay(ZONE).toInstant();

        return new Summary(
                payments.sumAmount(),
                payments.sumAmountSince(startOfMonth),
                payments.sumAmountSince(startOfToday),
                payments.count());
    }

    @Transactional(readOnly = true)
    public List<Payment> list(LocalDate from, LocalDate to) {
        Instant start = from == null ? null : from.atStartOfDay(ZONE).toInstant();
        Instant end = to == null ? null : to.plusDays(1).atStartOfDay(ZONE).toInstant();

        if (start != null && end != null) {
            return payments.findByPaidAtGreaterThanEqualAndPaidAtLessThanOrderByPaidAtDesc(start, end);
        }
        if (start != null) {
            return payments.findByPaidAtGreaterThanEqualOrderByPaidAtDesc(start);
        }
        if (end != null) {
            return payments.findByPaidAtLessThanOrderByPaidAtDesc(end);
        }
        return payments.findAllByOrderByPaidAtDesc();
    }
}
