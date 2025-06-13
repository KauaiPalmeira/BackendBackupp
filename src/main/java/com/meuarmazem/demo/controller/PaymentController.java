package com.meuarmazem.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static class PaymentRequest {
        public String title;
        public String origin;
        public java.math.BigDecimal amount;
        public String paymentDatetime; // ISO 8601 string
        public String description;
        public String paymentType;
    }

    @PostMapping
    public ResponseEntity<?> registerPayment(@RequestBody PaymentRequest paymentRequest, @RequestAttribute("userId") Long userId) {
        LocalDateTime dateTime = LocalDateTime.parse(paymentRequest.paymentDatetime, DateTimeFormatter.ISO_DATE_TIME);

        String sql = "INSERT INTO payments (user_id, title, origin, amount, payment_datetime, description, payment_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            userId,
            paymentRequest.title,
            paymentRequest.origin,
            paymentRequest.amount,
            dateTime,
            paymentRequest.description,
            paymentRequest.paymentType
        );

        return ResponseEntity.ok(Collections.singletonMap("message", "Pagamento registrado com sucesso"));
    }

    @GetMapping
    public List<Map<String, Object>> getPaymentsByDate(@RequestParam String date, @RequestAttribute("userId") Long userId) {
        String sql = "SELECT * FROM payments WHERE payment_datetime::date = CAST(? AS DATE) AND user_id = ? ORDER BY payment_datetime ASC";
        return jdbcTemplate.queryForList(sql, date, userId);
    }
} 