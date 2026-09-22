package com.logistica.sistema.repository;

import com.logistica.sistema.model.CronogramaEtapa;
import com.logistica.sistema.model.EtapaProducao;
import com.logistica.sistema.model.StatusEtapa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CronogramaEtapaRepository extends JpaRepository<CronogramaEtapa, Long> {

    List<CronogramaEtapa> findBySerialIdOrderByIdAsc(Long serialId);

    // Carregamento em lote das etapas de varios seriais (evita N+1 na
    // tela detalhada da OS, que mostra todos os seriais de uma vez).
    List<CronogramaEtapa> findBySerialIdInOrderByIdAsc(List<Long> serialIds);

    long countBySerialIdAndStatus(Long serialId, StatusEtapa status);

    boolean existsBySerialIdAndEtapa(Long serialId, EtapaProducao etapa);
}
