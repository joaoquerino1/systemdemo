package com.logistica.sistema.controller;

import com.logistica.sistema.dto.AtivoResponse;
import com.logistica.sistema.dto.AtualizarAtivoRequest;
import com.logistica.sistema.dto.CriarAtivoRequest;
import com.logistica.sistema.model.StatusAtivo;
import com.logistica.sistema.model.TipoAtivo;
import com.logistica.sistema.service.AtivoService;
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
@RequestMapping("/api/ativos")
@RequiredArgsConstructor
public class AtivoController {

    private final AtivoService ativoService;

    // Cadastro de ativo e uma acao administrativa - qualquer funcionario
    // pode CONSULTAR ativos (para retirar/devolver), mas so ADMIN/GESTOR
    // podem cadastrar, editar ou desativar.
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<AtivoResponse> criar(@Valid @RequestBody CriarAtivoRequest request) {
        var resposta = ativoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    public Page<AtivoResponse> listar(
            @RequestParam(required = false) TipoAtivo tipo,
            @RequestParam(required = false) StatusAtivo status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "codigo") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ativoService.listar(tipo, status, pageable);
    }

    @GetMapping("/stats")
    public Map<String, Object> estatisticas() {
        return ativoService.obterEstatisticas();
    }

    @GetMapping("/{codigo}")
    public AtivoResponse buscarPorCodigo(@PathVariable String codigo) {
        return ativoService.buscarPorCodigo(codigo);
    }

    @PutMapping("/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public AtivoResponse atualizar(@PathVariable String codigo, @Valid @RequestBody AtualizarAtivoRequest request) {
        return ativoService.atualizar(codigo, request);
    }

    @PatchMapping("/{codigo}/desativar")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<Void> desativar(@PathVariable String codigo) {
        ativoService.desativar(codigo);
        return ResponseEntity.noContent().build();
    }
}
