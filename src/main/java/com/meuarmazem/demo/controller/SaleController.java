package com.meuarmazem.demo.controller;

import com.meuarmazem.demo.model.Product;
import com.meuarmazem.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static class SaleRequest {
        public Long productId;
        public Integer quantity;
        public String saleDatetime; // ISO 8601 string
        public String description;
        public List<String> paymentMethods;
        public BigDecimal productPrice;
    }

    @PostMapping
    public ResponseEntity<?> registerSale(@RequestBody SaleRequest saleRequest, @RequestAttribute("userId") Long userId) {
        // 1. Buscar produto
        Optional<Product> productOpt = productRepository.findById(saleRequest.productId);
        if (productOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Produto não encontrado");
        }
        Product product = productOpt.get();

        // 2. Verificar se o produto pertence ao usuário
        if (!product.getUser().getId().equals(userId)) {
            return ResponseEntity.badRequest().body("Produto não pertence ao usuário");
        }

        // 3. Verificar estoque
        if (product.getQuantity() < saleRequest.quantity) {
            return ResponseEntity.badRequest().body("Estoque insuficiente");
        }

        // 4. Abater estoque
        product.setQuantity(product.getQuantity() - saleRequest.quantity);
        productRepository.save(product);

        // 5. Salvar venda
        String paymentMethodsStr = String.join(",", saleRequest.paymentMethods);
        LocalDateTime dateTime = LocalDateTime.parse(saleRequest.saleDatetime, DateTimeFormatter.ISO_DATE_TIME);

        String sql = "INSERT INTO sales (product_id, user_id, quantity, sale_datetime, description, payment_methods, product_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            saleRequest.productId,
            userId,
            saleRequest.quantity,
            dateTime,
            saleRequest.description,
            paymentMethodsStr,
            saleRequest.productPrice
        );

        return ResponseEntity.ok(Collections.singletonMap("message", "Venda registrada com sucesso"));
    }

    @GetMapping
    public List<Map<String, Object>> getSalesByDate(@RequestParam String date, @RequestAttribute("userId") Long userId) {
        String sql = "SELECT s.*, p.name as product_name FROM sales s JOIN products p ON s.product_id = p.id WHERE s.sale_datetime::date = CAST(? AS DATE) AND s.user_id = ? ORDER BY s.sale_datetime ASC";
        return jdbcTemplate.queryForList(sql, date, userId);
    }
} 