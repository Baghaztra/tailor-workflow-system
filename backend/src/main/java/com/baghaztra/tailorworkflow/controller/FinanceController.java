package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.entity.Payment;
import com.baghaztra.tailorworkflow.service.FinanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FinanceController {

    private final FinanceService finance;

    public FinanceController(FinanceService finance) {
        this.finance = finance;
    }

    @GetMapping("/finance/summary")
    public SummaryResponse summary() {
        return SummaryResponse.from(finance.summary());
    }

    @GetMapping("/payments")
    public List<PaymentResponse> payments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return finance.list(from, to).stream().map(PaymentResponse::from).toList();
    }

    public record SummaryResponse(
            BigDecimal totalRevenue,
            BigDecimal monthRevenue,
            BigDecimal todayRevenue,
            long paymentCount) {

        static SummaryResponse from(FinanceService.Summary summary) {
            return new SummaryResponse(
                    summary.totalRevenue(),
                    summary.monthRevenue(),
                    summary.todayRevenue(),
                    summary.paymentCount());
        }
    }

    public record PaymentResponse(
            Long id,
            Long orderId,
            String customerName,
            BigDecimal amount,
            Payment.Method method,
            Instant paidAt) {

        static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getOrder().getId(),
                    payment.getOrder().getCustomerName(),
                    payment.getAmount(),
                    payment.getMethod(),
                    payment.getPaidAt());
        }
    }
}
