package com.baghaztra.tailorworkflow.service;

import com.baghaztra.tailorworkflow.entity.Payment;
import com.baghaztra.tailorworkflow.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    @Transactional(readOnly = true)
    public Page<Payment> page(LocalDate from, LocalDate to, int page, int size) {
        Instant start = from == null ? null : from.atStartOfDay(ZONE).toInstant();
        Instant end = to == null ? null : to.plusDays(1).atStartOfDay(ZONE).toInstant();
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "paidAt"));

        if (start != null && end != null) {
            return payments.findByPaidAtGreaterThanEqualAndPaidAtLessThan(start, end, pageable);
        }
        if (start != null) {
            return payments.findByPaidAtGreaterThanEqual(start, pageable);
        }
        if (end != null) {
            return payments.findByPaidAtLessThan(end, pageable);
        }
        return payments.findAll(pageable);
    }

    public record DailyReport(LocalDate date, long paymentCount, BigDecimal revenue) {
    }

    @Transactional(readOnly = true)
    public List<DailyReport> report(LocalDate from, LocalDate to) {
        Map<LocalDate, DailyReport> rows = new TreeMap<>();
        for (Payment payment : list(from, to)) {
            LocalDate date = payment.getPaidAt().atZone(ZONE).toLocalDate();
            rows.merge(
                    date,
                    new DailyReport(date, 1, payment.getAmount()),
                    (left, right) -> new DailyReport(
                            date,
                            left.paymentCount() + right.paymentCount(),
                            left.revenue().add(right.revenue())));
        }
        return new ArrayList<>(rows.values());
    }

    @Transactional(readOnly = true)
    public String exportCsv(LocalDate from, LocalDate to) {
        StringBuilder csv = new StringBuilder("order_id,customer,amount,method,paid_at\n");
        for (Payment payment : list(from, to)) {
            csv.append(payment.getOrder().getId()).append(',')
                    .append(escapeCsv(payment.getOrder().getCustomerName())).append(',')
                    .append(payment.getAmount().toPlainString()).append(',')
                    .append(payment.getMethod()).append(',')
                    .append(payment.getPaidAt()).append('\n');
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
