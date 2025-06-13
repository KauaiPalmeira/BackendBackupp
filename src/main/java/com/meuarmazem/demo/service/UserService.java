package com.meuarmazem.demo.service;
import com.meuarmazem.demo.model.User;
import com.meuarmazem.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final Path profileImageStoragePath = Paths.get("uploads/profile-images");

    public UserService() {
        try {
            Files.createDirectories(profileImageStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório de imagens de perfil", e);
        }
    }

    public User registerUser(User user) {
        // Não codifica a senha novamente, pois já está codificada
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByDocumento(String documento) {
        return userRepository.existsByDocumento(documento);
    }

    @Transactional
    public User updateProfileImage(Long userId, MultipartFile profileImageFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (profileImageFile != null && !profileImageFile.isEmpty()) {
            // Deleta a imagem antiga se existir
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                try {
                    Path oldImagePath = profileImageStoragePath.resolve(user.getProfileImageUrl().replace("/profile-images/", ""));
                    Files.deleteIfExists(oldImagePath);
                } catch (IOException e) {
                    System.err.println("Não foi possível deletar a imagem de perfil antiga: " + e.getMessage());
                }
            }

            // Salva a nova imagem
            String fileName = System.currentTimeMillis() + "_" + profileImageFile.getOriginalFilename();
            Path filePath = profileImageStoragePath.resolve(fileName);
            Files.copy(profileImageFile.getInputStream(), filePath);
            user.setProfileImageUrl("/profile-images/" + fileName);
        }
        return userRepository.save(user);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
}