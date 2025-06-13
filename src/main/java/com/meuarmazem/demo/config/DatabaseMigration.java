package com.meuarmazem.demo.config;

import com.meuarmazem.demo.model.Product;
import com.meuarmazem.demo.model.User;
import com.meuarmazem.demo.repository.ProductRepository;
import com.meuarmazem.demo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

@Component
public class DatabaseMigration implements ApplicationRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        migrateProducts();
        migrateSalesTable();
        migratePaymentsTable();
    }

    public void migrateProducts() {
        System.out.println("Iniciando a migração de produtos...");

        User adminUser = userRepository.findByEmail("admin@example.com")
                .orElseGet(() -> {
                    Optional<User> firstUser = userRepository.findAll().stream().findFirst();
                    return firstUser.orElseThrow(() -> new RuntimeException("Nenhum usuário encontrado para migração."));
                });

        List<Product> productsWithoutUser = productRepository.findAll().stream()
                .filter(product -> product.getUser() == null)
                .toList();

        if (productsWithoutUser.isEmpty()) {
            System.out.println("Nenhum produto sem usuário encontrado para migração.");
            return;
        }

        System.out.println(String.format("Migrando %d produtos para o usuário: %s", productsWithoutUser.size(), adminUser.getEmail()));

        for (Product product : productsWithoutUser) {
            product.setUser(adminUser);
            productRepository.save(product);
            System.out.println(String.format("Produto '%s' migrado para o usuário %s", product.getName(), adminUser.getEmail()));
        }

        System.out.println("Migração de produtos concluída.");
    }

    public void migrateSalesTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS sales (
                id SERIAL PRIMARY KEY,
                product_id INTEGER NOT NULL REFERENCES products(id),
                user_id INTEGER NOT NULL REFERENCES users(id),
                quantity INTEGER NOT NULL,
                sale_datetime TIMESTAMP NOT NULL,
                description TEXT,
                payment_methods VARCHAR(255),
                product_price DECIMAL(10,2) NOT NULL
            );
        """;
        jdbcTemplate.execute(sql);
        System.out.println("Tabela 'sales' verificada/criada.");
    }

    public void migratePaymentsTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS payments (
                id SERIAL PRIMARY KEY,
                user_id INTEGER NOT NULL REFERENCES users(id),
                title VARCHAR(255) NOT NULL,
                origin VARCHAR(255) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                payment_datetime TIMESTAMP NOT NULL,
                description TEXT,
                payment_type VARCHAR(50) NOT NULL
            );
        """;
        jdbcTemplate.execute(sql);
        System.out.println("Tabela 'payments' verificada/criada.");
    }
} 