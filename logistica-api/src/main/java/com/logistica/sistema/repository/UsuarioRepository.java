package com.logistica.sistema.repository;

import com.logistica.sistema.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByMatricula(String matricula);

    Page<Usuario> findAll(Pageable pageable);

    // Contagem de usuarios ativos (para dashboard)
    long countByAtivoTrue();
}
