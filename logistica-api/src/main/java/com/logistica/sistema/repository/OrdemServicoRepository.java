package com.logistica.sistema.repository;

import com.logistica.sistema.model.OrdemServico;
import com.logistica.sistema.model.StatusOS;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    boolean existsByNumero(String numero);

    Page<OrdemServico> findByStatus(StatusOS status, Pageable pageable);

    @Query("""
        SELECT o FROM OrdemServico o
        WHERE LOWER(o.numero) LIKE LOWER(CONCAT('%', :termo, '%'))
           OR LOWER(COALESCE(o.descricao, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
    """)
    Page<OrdemServico> buscarPorTermo(@Param("termo") String termo, Pageable pageable);

    @Query("""
        SELECT o FROM OrdemServico o
        WHERE o.status = :status
          AND (LOWER(o.numero) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(o.descricao, '')) LIKE LOWER(CONCAT('%', :termo, '%')))
    """)
    Page<OrdemServico> buscarPorStatusETermo(@Param("status") StatusOS status, @Param("termo") String termo, Pageable pageable);
}

