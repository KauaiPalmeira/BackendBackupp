package com.meuarmazem.demo.config;

import com.meuarmazem.demo.model.Brand;
import com.meuarmazem.demo.model.Category;
import com.meuarmazem.demo.model.User;
import com.meuarmazem.demo.repository.BrandRepository;
import com.meuarmazem.demo.repository.CategoryRepository;
import com.meuarmazem.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DefaultDataInitializer {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initializeDefaultData() {
        initializeCategories();
        initializeBrands();
        initializeDefaultUser();
    }

    private void initializeCategories() {
        if (categoryRepository.count() == 0) {
            System.out.println("Inicializando categorias padrão...");
            
            List<Category> defaultCategories = Arrays.asList(
                Category.builder().name("Mouses").build(),
                Category.builder().name("Teclados").build(),
                Category.builder().name("Monitores").build(),
                Category.builder().name("Notebooks").build(),
                Category.builder().name("Headsets").build(),
                Category.builder().name("Webcams").build(),
                Category.builder().name("Caixas de Som").build(),
                Category.builder().name("Microfones").build(),
                Category.builder().name("Placas de Vídeo").build(),
                Category.builder().name("Fontes de Alimentação").build(),
                Category.builder().name("Gabinetes").build(),
                Category.builder().name("Memórias RAM").build(),
                Category.builder().name("Processadores").build(),
                Category.builder().name("Placas-mãe").build(),
                Category.builder().name("HDs").build(),
                Category.builder().name("SSDs").build(),
                Category.builder().name("Coolers").build(),
                Category.builder().name("Impressoras").build(),
                Category.builder().name("Scanners").build(),
                Category.builder().name("Estabilizadores").build()
            );

            categoryRepository.saveAll(defaultCategories);
            System.out.println("Categorias padrão inicializadas com sucesso!");
        }
    }

    private void initializeBrands() {
        if (brandRepository.count() == 0) {
            System.out.println("Inicializando marcas padrão...");
            
            List<Brand> defaultBrands = Arrays.asList(
                Brand.builder().name("Logitech").build(),
                Brand.builder().name("Razer").build(),
                Brand.builder().name("Corsair").build(),
                Brand.builder().name("HyperX").build(),
                Brand.builder().name("Asus").build(),
                Brand.builder().name("MSI").build(),
                Brand.builder().name("Dell").build(),
                Brand.builder().name("HP").build(),
                Brand.builder().name("Lenovo").build(),
                Brand.builder().name("Samsung").build()
            );

            brandRepository.saveAll(defaultBrands);
            System.out.println("Marcas padrão inicializadas com sucesso!");
        }
    }

    private void initializeDefaultUser() {
        if (userRepository.count() == 0) {
            System.out.println("Inicializando usuário padrão...");
            User defaultUser = User.builder()
                    .nomeUsuario("Admin")
                    .documento("000.000.000-00") // Ou um CNPJ válido
                    .email("admin@example.com")
                    .senha(passwordEncoder.encode("admin123"))
                    .cep("12345-678")
                    .estado("SP")
                    .municipio("Sao Paulo")
                    .bairro("Centro")
                    .rua("Rua Principal")
                    .dataCadastro(LocalDateTime.now())
                    .build();
            userRepository.save(defaultUser);
            System.out.println("Usuário padrão inicializado com sucesso!");
        }
    }
} 