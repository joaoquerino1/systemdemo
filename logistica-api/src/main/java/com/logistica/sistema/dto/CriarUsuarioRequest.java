package com.logistica.sistema.dto;

import com.logistica.sistema.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(
        @NotBlank String nome,
        @NotBlank @Size(min = 11, max = 11) String cpf,
        @NotBlank String matricula,
        String cargo,
        String setor,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "A senha deve ter pelo menos 8 caracteres") String senha,
        // ignorado no bootstrap do primeiro admin (sempre vira ADMIN);
        // usado normalmente quando um ADMIN cria outros usuários
        Role role
) {
}
