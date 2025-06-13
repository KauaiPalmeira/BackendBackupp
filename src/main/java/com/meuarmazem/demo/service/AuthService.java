package com.meuarmazem.demo.service;
import com.meuarmazem.demo.dto.LoginRequest;
import com.meuarmazem.demo.dto.LoginResponse;
import com.meuarmazem.demo.dto.RegisterRequest;
import com.meuarmazem.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest loginRequest) {
        logger.info("Tentativa de login para email: {}", loginRequest.getEmail());
        
        Optional<User> userOpt = userService.findByEmail(loginRequest.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.info("Usuário encontrado: {}", user.getEmail());
            logger.info("Senha fornecida: {}", loginRequest.getSenha());
            logger.info("Hash da senha no banco: {}", user.getSenha());
            
            // Teste de codificação
            String testHash = passwordEncoder.encode(loginRequest.getSenha());
            logger.info("Hash de teste da senha fornecida: {}", testHash);
            logger.info("Comparação com hash de teste: {}", passwordEncoder.matches(loginRequest.getSenha(), testHash));
            
            boolean matches = passwordEncoder.matches(loginRequest.getSenha(), user.getSenha());
            logger.info("Senha corresponde: {}", matches);
            
            if (matches) {
                return new LoginResponse("Login bem-sucedido", "Token placeholder");
            }
        }
        return new LoginResponse(null, "Credenciais inválidas");
    }

    public User register(RegisterRequest registerRequest) {
        // Validações
        if (!registerRequest.getSenha().equals(registerRequest.getConfirmarSenha())) {
            throw new RuntimeException("As senhas não coincidem");
        }

        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (userService.existsByDocumento(registerRequest.getDocumento())) {
            throw new RuntimeException("CPF/CNPJ já cadastrado");
        }

        // Criar usuário
        String encodedPassword = passwordEncoder.encode(registerRequest.getSenha());
        logger.info("=== PROCESSO DE REGISTRO ===");
        logger.info("Senha original: {}", registerRequest.getSenha());
        logger.info("Senha codificada: {}", encodedPassword);
        logger.info("Teste de verificação: {}", passwordEncoder.matches(registerRequest.getSenha(), encodedPassword));

        User user = User.builder()
                .nomeUsuario(registerRequest.getNomeUsuario())
                .senha(encodedPassword)
                .email(registerRequest.getEmail())
                .documento(registerRequest.getDocumento())
                .cep(registerRequest.getCep())
                .estado(registerRequest.getEstado())
                .municipio(registerRequest.getMunicipio())
                .bairro(registerRequest.getBairro())
                .rua(registerRequest.getRua())
                .build();

        User savedUser = userService.registerUser(user);
        logger.info("Hash da senha após salvar: {}", savedUser.getSenha());
        logger.info("Teste final de verificação: {}", passwordEncoder.matches(registerRequest.getSenha(), savedUser.getSenha()));
        return savedUser;
    }

    public User getUserByEmail(String email) {
        return userService.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
