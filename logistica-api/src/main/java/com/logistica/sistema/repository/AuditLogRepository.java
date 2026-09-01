package com.logistica.sistema.repository;

import com.logistica.sistema.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findTop20ByOrderByCriadoEmDesc();

    List<AuditLog> findByEntidadeAndEntidadeIdOrderByCriadoEmDesc(String entidade, Long entidadeId);
}
