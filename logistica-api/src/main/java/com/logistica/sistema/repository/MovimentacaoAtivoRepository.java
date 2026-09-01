package com.logistica.sistema.repository;

import com.logistica.sistema.model.MovimentacaoAtivo;
import com.logistica.sistema.model.StatusMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovimentacaoAtivoRepository extends JpaRepository<MovimentacaoAtivo, Long> {

    // Usado para checar/obter a movimentacao aberta de um ativo antes
    // de permitir uma nova retirada ou processar uma devolucao.
    Optional<MovimentacaoAtivo> findByAtivoIdAndStatus(Long ativoId, StatusMovimentacao status);

    List<MovimentacaoAtivo> findByUsuarioIdOrderByDataHoraRetiradaDesc(Long usuarioId);

    List<MovimentacaoAtivo> findByAtivoIdOrderByDataHoraRetiradaDesc(Long ativoId);

    List<MovimentacaoAtivo> findByStatus(StatusMovimentacao status);

    // Contagem de movimentacoes abertas (para dashboard)
    long countByStatus(StatusMovimentacao status);

    // Ultimas N movimentacoes (para dashboard)
    List<MovimentacaoAtivo> findTop10ByOrderByDataHoraRetiradaDesc();
}
