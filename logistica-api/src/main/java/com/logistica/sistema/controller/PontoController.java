package com.logistica.sistema.controller;

import com.logistica.sistema.dto.RegistroPontoResponse;
import com.logistica.sistema.security.UsuarioDetails;
import com.logistica.sistema.service.FolhaHoraService;
import com.logistica.sistema.service.RegistroPontoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ponto")
@RequiredArgsConstructor
public class PontoController {

    private final RegistroPontoService registroPontoService;
    private final FolhaHoraService folhaHoraService;

    // O usuarioId nunca vem da requisicao aqui - sempre do token JWT
    // do usuario autenticado, porque bater ponto e uma acao pessoal
    // (diferente da retirada de ativo, que pode ser feita em nome de
    // outro funcionario por decisao de negocio).
    @PostMapping("/entrada")
    public RegistroPontoResponse registrarEntrada(@AuthenticationPrincipal UsuarioDetails usuarioDetails) {
        return registroPontoService.registrarEntrada(usuarioDetails.getUsuario().getId());
    }

    @PostMapping("/saida")
    public RegistroPontoResponse registrarSaida(@AuthenticationPrincipal UsuarioDetails usuarioDetails) {
        return registroPontoService.registrarSaida(usuarioDetails.getUsuario().getId());
    }

    @PostMapping("/intervalo/saida")
    public RegistroPontoResponse registrarSaidaIntervalo(@AuthenticationPrincipal UsuarioDetails usuarioDetails) {
        return registroPontoService.registrarSaidaIntervalo(usuarioDetails.getUsuario().getId());
    }

    @PostMapping("/intervalo/volta")
    public RegistroPontoResponse registrarVoltaIntervalo(@AuthenticationPrincipal UsuarioDetails usuarioDetails) {
        return registroPontoService.registrarVoltaIntervalo(usuarioDetails.getUsuario().getId());
    }

    // Historico do proprio usuario logado. Se nao vier periodo, assume
    // o mes corrente - o caso de uso mais comum (conferir o proprio ponto).
    @GetMapping("/meus")
    public List<RegistroPontoResponse> meusRegistros(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        LocalDate hoje = LocalDate.now();
        LocalDate dataInicio = inicio != null ? inicio : hoje.withDayOfMonth(1);
        LocalDate dataFim = fim != null ? fim : hoje;
        return registroPontoService.listarPorPeriodo(
                usuarioDetails.getUsuario().getId(), dataInicio, dataFim);
    }

    // Consulta do ponto de qualquer funcionario - uso administrativo
    // (ex: conferencia antes de gerar a folha de hora de alguem).
    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN','GESTOR')")
    public List<RegistroPontoResponse> registrosDoUsuario(
            @PathVariable Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return registroPontoService.listarPorPeriodo(usuarioId, inicio, fim);
    }

    /**
     * Segunda via da folha de hora em PDF. Sem 'usuarioId', gera a do
     * proprio usuario logado (uso mais comum: funcionario pedindo sua
     * segunda via). Gerar a folha de outro usuario exige ADMIN/GESTOR.
     */
    @GetMapping("/folha-hora")
    public ResponseEntity<byte[]> folhaHora(
            @AuthenticationPrincipal UsuarioDetails usuarioDetails,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        Long idAlvo = usuarioId != null ? usuarioId : usuarioDetails.getUsuario().getId();

        boolean solicitandoDeOutro = !idAlvo.equals(usuarioDetails.getUsuario().getId());
        boolean temPermissaoAdministrativa = usuarioDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_GESTOR"));
        if (solicitandoDeOutro && !temPermissaoAdministrativa) {
            throw new AccessDeniedException("Você só pode gerar a folha de hora da sua própria conta.");
        }

        byte[] pdf = folhaHoraService.gerarPdf(idAlvo, inicio, fim);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=folha-hora.pdf")
                .body(pdf);
    }
}
