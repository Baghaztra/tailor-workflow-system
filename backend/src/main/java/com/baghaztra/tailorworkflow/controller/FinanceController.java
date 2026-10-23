package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.entity.Payment;
import com.baghaztra.tailorworkflow.service.FinanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    @GetMapping("/finance/report")
    public List<DailyReportResponse> report(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return finance.report(from, to).stream().map(DailyReportResponse::from).toList();
    }

    @GetMapping("/finance/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        String filename = "payments-" + (from == null ? "all" : from) + "-" + (to == null ? "all" : to) + ".csv";
        byte[] body = finance.exportCsv(from, to).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
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

    public record DailyReportResponse(LocalDate date, long paymentCount, BigDecimal revenue) {

        static DailyReportResponse from(FinanceService.DailyReport report) {
            return new DailyReportResponse(report.date(), report.paymentCount(), report.revenue());
        }
    }
}
