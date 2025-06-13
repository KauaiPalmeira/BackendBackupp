package com.meuarmazem.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String nomeUsuario;
    private String email;
    private String documento;
    private String cep;
    private String estado;
    private String municipio;
    private String bairro;
    private String rua;
    private String senhaMascarada; // Para exibir ****
    private String profileImageUrl;
} 