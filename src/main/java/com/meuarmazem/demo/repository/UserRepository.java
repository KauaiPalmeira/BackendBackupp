package com.meuarmazem.demo.repository;

import com.meuarmazem.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByDocumento(String documento);
    boolean existsByEmail(String email);
    boolean existsByDocumento(String documento);
}
