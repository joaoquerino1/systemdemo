package com.logistica.sistema.repository;

import com.logistica.sistema.model.Ativo;
import com.logistica.sistema.model.StatusAtivo;
import com.logistica.sistema.model.TipoAtivo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AtivoRepository extends JpaRepository<Ativo, Long> {

    Optional<Ativo> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    // --- Paginacao com optional filters (tipo, status) ---
    // Uma unica query JPQL trata os 4 casos (nenhum filtro, so tipo,
    // so status, ambos) sem filtering in-memory.
    @Query("SELECT a FROM Ativo a " +
           "WHERE (:tipo IS NULL OR a.tipo = :tipo) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "ORDER BY a.codigo")
    Page<Ativo> buscarComFiltros(@Param("tipo") TipoAtivo tipo,
                                 @Param("status") StatusAtivo status,
                                 Pageable pageable);

    // Contagem por status (para dashboard)
    @Query("SELECT a.status, COUNT(a) FROM Ativo a WHERE a.ativo = true GROUP BY a.status")
    List<Object[]> contarPorStatus();

    // Contagem por tipo (para dashboard)
    @Query("SELECT a.tipo, COUNT(a) FROM Ativo a WHERE a.ativo = true GROUP BY a.tipo")
    List<Object[]> contarPorTipo();
}
