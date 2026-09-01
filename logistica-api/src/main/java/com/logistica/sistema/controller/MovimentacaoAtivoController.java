package com.logistica.sistema.controller;

import com.logistica.sistema.dto.DevolucaoAtivoRequest;
import com.logistica.sistema.dto.MovimentacaoAtivoResponse;
import com.logistica.sistema.dto.RetiradaAtivoRequest;
import com.logistica.sistema.service.MovimentacaoAtivoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimentacoes")
@RequiredArgsConstructor
public class MovimentacaoAtivoController {

    private final MovimentacaoAtivoService movimentacaoService;

    @PostMapping("/retirada")
    public ResponseEntity<MovimentacaoAtivoResponse> retirar(
            @Valid @RequestBody RetiradaAtivoRequest request) {
        var resposta = movimentacaoService.registrarRetirada(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping("/devolucao")
    public ResponseEntity<MovimentacaoAtivoResponse> devolver(
            @Valid @RequestBody DevolucaoAtivoRequest request) {
        var resposta = movimentacaoService.registrarDevolucao(request);
        return ResponseEntity.ok(resposta);
    }
}
