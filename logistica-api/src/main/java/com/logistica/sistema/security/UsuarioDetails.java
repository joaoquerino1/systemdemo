package com.logistica.sistema.security;

import com.logistica.sistema.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapta a entidade Usuario para a interface que o Spring Security
 * espera. Mantemos o Usuario como entidade JPA "pura" (sem depender
 * de classes do Security) e concentramos a adaptacao aqui.
 */
public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // prefixo "ROLE_" e a convencao que o Spring Security espera
        // para usar hasRole("ADMIN") em vez de hasAuthority("ROLE_ADMIN")
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getSenhaHash();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isAtivo();
    }
}
