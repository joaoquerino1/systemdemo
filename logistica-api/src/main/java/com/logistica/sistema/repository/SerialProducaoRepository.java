package com.logistica.sistema.repository;

import com.logistica.sistema.model.SerialProducao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SerialProducaoRepository extends JpaRepository<SerialProducao, Long> {

    List<SerialProducao> findByOrdemServicoId(Long ordemServicoId);

    boolean existsByOrdemServicoIdAndCodigoSerial(Long ordemServicoId, String codigoSerial);

    long countByOrdemServicoIdAndStatus(Long ordemServicoId, com.logistica.sistema.model.StatusSerial status);

    // Contagem agregada de seriais por OS em uma unica query (evita
    // N+1 na listagem de OS, que mostra total/concluidos de cada uma).
    @Query("select s.ordemServico.id, s.status, count(s) from SerialProducao s " +
            "where s.ordemServico.id in :ids group by s.ordemServico.id, s.status")
    List<Object[]> contarSeriaisPorOS(@Param("ids") List<Long> ids);
}
