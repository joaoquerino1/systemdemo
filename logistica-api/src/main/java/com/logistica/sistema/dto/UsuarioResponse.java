package com.logistica.sistema.dto;

import com.logistica.sistema.model.Role;
import com.logistica.sistema.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String cargo,
        String setor,
        Role role,
        boolean ativo
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNome(), u.getEmail(), u.getCargo(),
                u.getSetor(), u.getRole(), u.isAtivo());
    }
}
