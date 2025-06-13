package com.meuarmazem.demo.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String nomeUsuario;
    private String senha;
    private String confirmarSenha;
    private String email;
    private String documento;
    private String cep;
    private String estado;
    private String municipio;
    private String bairro;
    private String rua;
}