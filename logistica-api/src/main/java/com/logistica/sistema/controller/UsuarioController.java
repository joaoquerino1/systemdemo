package com.logistica.sistema.controller;

import com.logistica.sistema.dto.CriarUsuarioRequest;
import com.logistica.sistema.dto.UsuarioResponse;
import com.logistica.sistema.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // usada para popular a selecao de funcionario nas telas
    // administrativas (ex: gerar folha de hora de outro usuario)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public Page<UsuarioResponse> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "nome") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return usuarioService.listar(pageable);
    }

    /**
     * Endpoint publico, mas so funciona uma vez: enquanto o banco
     * estiver sem nenhum usuario. Depois disso, sempre retorna erro
     * (ver UsuarioService.criarPrimeiroAdmin). E a unica rota de
     * criacao de usuario que nao exige estar autenticado.
     */
    @PostMapping("/bootstrap-admin")
    public ResponseEntity<UsuarioResponse> bootstrapAdmin(
            @Valid @RequestBody CriarUsuarioRequest request) {
        var resposta = usuarioService.criarPrimeiroAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> criar(
            @Valid @RequestBody CriarUsuarioRequest request) {
        var resposta = usuarioService.criarUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    // Dashboard stats
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public Map<String, Object> estatisticas() {
        return usuarioService.obterEstatisticas();
    }
}
