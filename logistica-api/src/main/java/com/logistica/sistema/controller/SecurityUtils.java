package com.logistica.sistema.controller;

import com.logistica.sistema.security.UsuarioDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helpers de permissao compartilhados pelos controllers.
 */
final class SecurityUtils {

    private SecurityUtils() {
    }

    static boolean eAdminOuGestor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority a : auth.getAuthorities()) {
            String nome = a.getAuthority();
            if (nome.equals("ROLE_ADMIN") || nome.equals("ROLE_GESTOR")) {
                return true;
            }
        }
        return false;
    }
}
