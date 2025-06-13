package com.meuarmazem.demo.dto;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String message;

    // Construtor com dois argumentos
    public LoginResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    // Construtor vazio (se necessário)
    public LoginResponse() {
    }

    // Getters e Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
