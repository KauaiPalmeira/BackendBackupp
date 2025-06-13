package com.meuarmazem.demo.service;

import com.meuarmazem.demo.model.Product;
import com.meuarmazem.demo.model.User;
import com.meuarmazem.demo.repository.ProductRepository;
import com.meuarmazem.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private final Path imageStoragePath;

    @Autowired
    public ProductService(@Value("${app.base.upload.dir}") String baseUploadDir) {
        this.imageStoragePath = Paths.get(baseUploadDir, "images");
        try {
            Files.createDirectories(this.imageStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de imagens", e);
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @Transactional
    public Product saveProduct(Product product, MultipartFile imageFile) throws IOException {
        User currentUser = getCurrentUser();
        product.setUser(currentUser);

        // Definir valores padrão se não forem fornecidos (ou garantir que venham do DTO)
        if (product.getQuantity() == null) {
            product.setQuantity(0); // Valor padrão
        }
        if (product.getStatus() == null) {
            product.setStatus(com.meuarmazem.demo.model.ProductStatus.INDISPONIVEL); // Valor padrão
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
            Path filePath = imageStoragePath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath);
            product.setImageUrl("/images/" + fileName);
        }

        return productRepository.save(product);
    }

    public List<Product> findAll() {
        User currentUser = getCurrentUser();
        return productRepository.findByUserId(currentUser.getId());
    }

    public Optional<Product> findById(Long id) {
        User currentUser = getCurrentUser();
        return productRepository.findByIdAndUserId(id, currentUser.getId());
    }

    @Transactional
    public void deleteById(Long id) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (product.getImageUrl() != null) {
            try {
                Path imagePath = imageStoragePath.resolve(product.getImageUrl().replace("/images/", ""));
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao deletar imagem do produto", e);
            }
        }

        productRepository.delete(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct, MultipartFile newImageFile) throws IOException {
        User currentUser = getCurrentUser();
        Product existingProduct = productRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Atualiza os campos do produto
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setFinalPrice(updatedProduct.getFinalPrice());
        existingProduct.setCost(updatedProduct.getCost());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setBrand(updatedProduct.getBrand());
        existingProduct.setQuantity(updatedProduct.getQuantity()); // Atualiza quantidade
        existingProduct.setStatus(updatedProduct.getStatus()); // Atualiza status

        // Atualiza a imagem se uma nova foi fornecida
        if (newImageFile != null && !newImageFile.isEmpty()) {
            // Deleta a imagem antiga se existir
            if (existingProduct.getImageUrl() != null) {
                Path oldImagePath = imageStoragePath.resolve(existingProduct.getImageUrl().replace("/images/", ""));
                Files.deleteIfExists(oldImagePath);
            }

            // Salva a nova imagem
            String fileName = System.currentTimeMillis() + "_" + newImageFile.getOriginalFilename();
            Path filePath = imageStoragePath.resolve(fileName);
            Files.copy(newImageFile.getInputStream(), filePath);
            existingProduct.setImageUrl("/images/" + fileName);
        }

        return productRepository.save(existingProduct);
    }
} 