package com.logistica.sistema.controller;

import com.logistica.sistema.dto.*;
import com.logistica.sistema.model.StatusOS;
import com.logistica.sistema.security.UsuarioDetails;
import com.logistica.sistema.service.ProducaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints do cronograma de producao. Padrao de permissao igual ao
 * do folha-hora (PontoController): acoes "em nome de outro usuario"
 * (informar colaborador diferente de si mesmo) exigem ADMIN/GESTOR.
 */
@RestController
@RequestMapping("/api/producao")
@RequiredArgsConstructor
public class ProducaoController {

    private final ProducaoService producaoService;

    @PostMapping("/ordens")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<OrdemServicoResponse> criarOS(@RequestBody CriarOrdemServicoRequest request) {
        OrdemServicoResponse response = producaoService.criarOS(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ordens")
    public Page<OrdemServicoResponse> listarOS(
            @RequestParam(required = false) StatusOS status,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "criadoEm"));
        return producaoService.listarOS(status, busca, pageable);
    }

    @GetMapping("/ordens/{id}")
    public OrdemServicoResponse detalharOS(@PathVariable Long id) {
        return producaoService.detalharOS(id);
    }

    @PostMapping("/ordens/{id}/seriais")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public ResponseEntity<SerialProducaoResponse> adicionarSerial(
            @PathVariable Long id,
            @RequestBody AdicionarSerialRequest request) {
        SerialProducaoResponse response = producaoService.adicionarSerial(id, request.codigoSerial());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Inicia uma etapa. Se colaboradorId vier e for diferente do
     * usuario autenticado, exige ADMIN/GESTOR.
     */
    @PostMapping("/etapas/{id}/iniciar")
    public EtapaResponse iniciarEtapa(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @PathVariable Long id,
            @RequestBody(required = false) IniciarEtapaRequest request) {

        Long idAutenticado = usuarioDetails.getUsuario().getId();
        Long colaboradorId = resolverColaborador(request != null ? request.colaboradorId() : null, idAutenticado);
        return producaoService.iniciarEtapa(id, colaboradorId);
    }

    @PostMapping("/etapas/{id}/concluir")
    public EtapaResponse concluirEtapa(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @PathVariable Long id,
            @RequestBody ConcluirEtapaRequest request) {
        return producaoService.concluirEtapa(id, request);
    }

    @PutMapping("/etapas/{id}/pendencias")
    public EtapaResponse registrarPendencia(
            @PathVariable Long id,
            @RequestBody PendenciaRequest request) {
        return producaoService.registrarPendencia(id, request.texto());
    }

    @PostMapping("/etapas/{id}/reabrir")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public EtapaResponse reabrirEtapa(
            @PathVariable Long id,
            @RequestBody ReabrirEtapaRequest request) {
        return producaoService.reabrirEtapa(id, request.motivo());
    }

    /** Se o alvo e outro usuario, somente ADMIN/GESTOR podem pedir. */
    private Long resolverColaborador(Long solicitado, Long idAutenticado) {
        if (solicitado == null || solicitado.equals(idAutenticado)) {
            return idAutenticado;
        }
        boolean isAdminOuGestor = SecurityUtils.eAdminOuGestor();
        if (!isAdminOuGestor) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Somente ADMIN/GESTOR podem iniciar uma etapa em nome de outro colaborador.");
        }
        return solicitado;
    }
}
