package com.meuarmazem.demo.controller;

import com.meuarmazem.demo.dto.ProductRequest;
import com.meuarmazem.demo.dto.ProductResponse;
import com.meuarmazem.demo.model.Brand;
import com.meuarmazem.demo.model.Category;
import com.meuarmazem.demo.model.Product;
import com.meuarmazem.demo.model.ProductStatus;
import com.meuarmazem.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    private ProductResponse convertToDto(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .finalPrice(product.getFinalPrice())
                .cost(product.getCost())
                .imageUrl(product.getImageUrl())
                .category(ProductResponse.CategoryDto.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build())
                .brand(ProductResponse.BrandDto.builder()
                        .id(product.getBrand().getId())
                        .name(product.getBrand().getName())
                        .build())
                .quantity(product.getQuantity())
                .status(product.getStatus())
                .build();
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestPart("product") ProductRequest productRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        logger.info("Recebida requisição para criar produto: {}", productRequest.getName());
        try {
            Category category = Category.builder().id(productRequest.getCategoryId()).build();
            Brand brand = Brand.builder().id(productRequest.getBrandId()).build();

            Product product = Product.builder()
                    .name(productRequest.getName())
                    .description(productRequest.getDescription())
                    .finalPrice(productRequest.getFinalPrice())
                    .cost(productRequest.getCost())
                    .category(category)
                    .brand(brand)
                    .quantity(productRequest.getQuantity() != null ? productRequest.getQuantity() : 0)
                    .status(productRequest.getStatus() != null ? productRequest.getStatus() : ProductStatus.INDISPONIVEL)
                    .build();

            Product savedProduct = productService.saveProduct(product, imageFile);
            logger.info("Produto salvo com sucesso: {}", savedProduct.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(savedProduct));
        } catch (RuntimeException | IOException e) {
            logger.error("Erro ao criar produto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Trate melhor os erros em produção
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        logger.info("Recebida requisição para listar produtos.");
        List<Product> products = productService.findAll();
        List<ProductResponse> productResponses = products.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        logger.info("Recebida requisição para buscar produto com ID: {}", id);
        Optional<Product> product = productService.findById(id);
        return product.map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("Produto com ID {} não encontrado.", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("Recebida requisição para excluir produto com ID: {}", id);
        try {
            productService.deleteById(id);
            logger.info("Produto com ID {} excluído com sucesso.", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Erro ao excluir produto com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") ProductRequest productRequest,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        logger.info("Recebida requisição para atualizar produto com ID: {}", id);
        try {
            Category category = Category.builder().id(productRequest.getCategoryId()).build();
            Brand brand = Brand.builder().id(productRequest.getBrandId()).build();

            Product productToUpdate = Product.builder()
                    .name(productRequest.getName())
                    .description(productRequest.getDescription())
                    .finalPrice(productRequest.getFinalPrice())
                    .cost(productRequest.getCost())
                    .category(category)
                    .brand(brand)
                    .quantity(productRequest.getQuantity())
                    .status(productRequest.getStatus())
                    .build();

            Product updatedProduct = productService.updateProduct(id, productToUpdate, imageFile);
            logger.info("Produto com ID {} atualizado com sucesso.", id);
            return ResponseEntity.ok(convertToDto(updatedProduct));
        } catch (RuntimeException | IOException e) {
            logger.error("Erro ao atualizar produto com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
} 