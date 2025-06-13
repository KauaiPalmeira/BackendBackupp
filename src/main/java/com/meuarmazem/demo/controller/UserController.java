package com.meuarmazem.demo.controller;

import com.meuarmazem.demo.dto.UserResponse;
import com.meuarmazem.demo.model.User;
import com.meuarmazem.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/details")
    public ResponseEntity<UserResponse> getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // O email do usuário é o principal no Spring Security

        Optional<User> userOpt = userService.findByEmail(userEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .nomeUsuario(user.getNomeUsuario())
                .email(user.getEmail())
                .documento(user.getDocumento())
                .cep(user.getCep())
                .estado(user.getEstado())
                .municipio(user.getMunicipio())
                .bairro(user.getBairro())
                .rua(user.getRua())
                .senhaMascarada("********") // Senha mascarada
                .profileImageUrl(user.getProfileImageUrl())
                .build();
            return ResponseEntity.ok(userResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{userId}/profile-image")
    public ResponseEntity<UserResponse> uploadProfileImage(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        try {
            // Verifique se o usuário logado é o mesmo que está tentando atualizar a imagem
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            User loggedInUser = userService.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado."));

            if (!loggedInUser.getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Não autorizado
            }

            User updatedUser = userService.updateProfileImage(userId, file);
            UserResponse userResponse = UserResponse.builder()
                .id(updatedUser.getId())
                .nomeUsuario(updatedUser.getNomeUsuario())
                .email(updatedUser.getEmail())
                .documento(updatedUser.getDocumento())
                .cep(updatedUser.getCep())
                .estado(updatedUser.getEstado())
                .municipio(updatedUser.getMunicipio())
                .bairro(updatedUser.getBairro())
                .rua(updatedUser.getRua())
                .senhaMascarada("********")
                .profileImageUrl(updatedUser.getProfileImageUrl())
                .build();
            return ResponseEntity.ok(userResponse);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Trate melhor os erros em produção
        }
    }
} 