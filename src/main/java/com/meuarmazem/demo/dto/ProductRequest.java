package com.meuarmazem.demo.dto;

import com.meuarmazem.demo.model.ProductStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String name;
    private String description;
    private Double finalPrice;
    private Double cost;
    private Long categoryId;
    private Long brandId;
    private Integer quantity;
    private ProductStatus status;
} 