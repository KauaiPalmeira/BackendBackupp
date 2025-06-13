package com.meuarmazem.demo.controller;

import com.meuarmazem.demo.dto.LoginRequest;
import com.meuarmazem.demo.dto.LoginResponse;
import com.meuarmazem.demo.dto.RegisterRequest;
import com.meuarmazem.demo.model.User;
import com.meuarmazem.demo.service.AuthService;
import com.meuarmazem.demo.service.JwtUserDetailsService;
import com.meuarmazem.demo.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticate(loginRequest.getEmail(), loginRequest.getSenha());

            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            com.meuarmazem.demo.model.User user = authService.getUserByEmail(loginRequest.getEmail());
            final String token = jwtTokenUtil.generateTokenWithUserId(userDetails, user.getId());

            return ResponseEntity.ok(new LoginResponse(token, "Login bem-sucedido"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", "Dados incorretos"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            User user = authService.register(registerRequest);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void authenticate(String email, String senha) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, senha));
        } catch (DisabledException e) {
            throw new Exception("USUÁRIO_DESABILITADO", e);
        } catch (BadCredentialsException e) {
            throw new Exception("CREDENCIAS_INVÁLIDAS", e);
        }
    }
}
