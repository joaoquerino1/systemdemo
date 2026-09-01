package com.logistica.sistema.service;

import com.logistica.sistema.dto.DevolucaoAtivoRequest;
import com.logistica.sistema.dto.MovimentacaoAtivoResponse;
import com.logistica.sistema.dto.RetiradaAtivoRequest;
import com.logistica.sistema.exception.RecursoNaoEncontradoException;
import com.logistica.sistema.exception.RegraNegocioException;
import com.logistica.sistema.model.*;
import com.logistica.sistema.repository.AtivoRepository;
import com.logistica.sistema.repository.AtivoVeiculoRepository;
import com.logistica.sistema.repository.MovimentacaoAtivoRepository;
import com.logistica.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MovimentacaoAtivoService {

    private final AtivoRepository ativoRepository;
    private final AtivoVeiculoRepository ativoVeiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimentacaoAtivoRepository movimentacaoRepository;
    private final AuditLogService auditLogService;

    /**
     * Registra a retirada de um ativo. Regras aplicadas:
     * - o ativo precisa existir e estar ativo (nao desativado no cadastro)
     * - o ativo nao pode ja estar em uso (nao pode haver movimentacao ABERTA)
     * - ao final, o status do ativo muda para EM_USO
     *
     * A checagem aqui e a garantia da aplicacao; o indice unico parcial
     * no banco (idx_um_aberto_por_ativo) e quem garante a regra mesmo
     * em caso de corrida entre duas requisicoes simultaneas.
     */
    @Transactional
    public MovimentacaoAtivoResponse registrarRetirada(RetiradaAtivoRequest request) {
        Ativo ativo = ativoRepository.findByCodigo(request.codigoAtivo())
                .filter(Ativo::isAtivo)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ativo nao encontrado ou inativo: " + request.codigoAtivo()));

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuario nao encontrado: " + request.usuarioId()));

        if (!usuario.isAtivo()) {
            throw new RegraNegocioException(
                    "Este usuario esta inativo e nao pode retirar ativos.");
        }

        boolean jaEmUso = movimentacaoRepository
                .findByAtivoIdAndStatus(ativo.getId(), StatusMovimentacao.ABERTO)
                .isPresent();
        if (jaEmUso) {
            throw new RegraNegocioException(
                    "Este ativo ja esta em uso e nao foi devolvido ainda.");
        }
        if (ativo.getStatus() == StatusAtivo.MANUTENCAO) {
            throw new RegraNegocioException(
                    "Este ativo esta em manutencao e nao pode ser retirado.");
        }

        MovimentacaoAtivo movimentacao = MovimentacaoAtivo.builder()
                .ativo(ativo)
                .usuario(usuario)
                .observacoes(request.observacoes())
                .build();
        movimentacao = movimentacaoRepository.save(movimentacao);

        ativo.setStatus(StatusAtivo.EM_USO);
        ativoRepository.save(ativo);

        // Se for veiculo e a requisicao informou km de saida, registra
        // no cadastro do veiculo (o km_atual do veiculo e a fonte da
        // verdade, nao a movimentacao)
        if (ativo.getTipo() == TipoAtivo.VEICULO && request.kmSaida() != null) {
            ativoVeiculoRepository.findById(ativo.getId()).ifPresent(av -> {
                av.setKmAtual(request.kmSaida());
                ativoVeiculoRepository.save(av);
            });
        }

        // Audit log
        auditLogService.registrar(
                "RETIRADA",
                "MOVIMENTACAO",
                movimentacao.getId(),
                String.format("Ativo: %s (%s) -> Usuario: %s (ID: %d)",
                        ativo.getNome(), ativo.getCodigo(),
                        usuario.getNome(), usuario.getId())
        );

        return MovimentacaoAtivoResponse.from(movimentacao);
    }

    /**
     * Registra a devolucao de um ativo. Regras aplicadas:
     * - precisa existir uma movimentacao ABERTA para o ativo
     * - ao final, o status do ativo volta para DISPONIVEL
     */
    @Transactional
    public MovimentacaoAtivoResponse registrarDevolucao(DevolucaoAtivoRequest request) {
        Ativo ativo = ativoRepository.findByCodigo(request.codigoAtivo())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ativo nao encontrado: " + request.codigoAtivo()));

        MovimentacaoAtivo movimentacao = movimentacaoRepository
                .findByAtivoIdAndStatus(ativo.getId(), StatusMovimentacao.ABERTO)
                .orElseThrow(() -> new RegraNegocioException(
                        "Nao ha retirada em aberto para este ativo."));

        movimentacao.setDataHoraDevolucao(LocalDateTime.now());
        movimentacao.setStatus(StatusMovimentacao.FECHADO);
        if (request.observacoes() != null) {
            String obsAtual = movimentacao.getObservacoes();
            movimentacao.setObservacoes(
                    obsAtual == null ? request.observacoes()
                            : obsAtual + " | Devolucao: " + request.observacoes());
        }
        movimentacao = movimentacaoRepository.save(movimentacao);

        ativo.setStatus(StatusAtivo.DISPONIVEL);
        ativoRepository.save(ativo);

        if (ativo.getTipo() == TipoAtivo.VEICULO && request.kmChegada() != null) {
            ativoVeiculoRepository.findById(ativo.getId()).ifPresent(av -> {
                av.setKmAtual(request.kmChegada());
                ativoVeiculoRepository.save(av);
            });
        }

        // Audit log
        auditLogService.registrar(
                "DEVOLUCAO",
                "MOVIMENTACAO",
                movimentacao.getId(),
                String.format("Ativo: %s (%s) devolvido",
                        ativo.getNome(), ativo.getCodigo())
        );

        return MovimentacaoAtivoResponse.from(movimentacao);
    }
}
