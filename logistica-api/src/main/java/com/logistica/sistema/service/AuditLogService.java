package com.logistica.sistema.service;

import com.logistica.sistema.model.AuditLog;
import com.logistica.sistema.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Registra uma acao de auditoria. O email do usuario e obtido
     * automaticamente do SecurityContext (token JWT).
     */
    @Transactional
    public void registrar(String acao, String entidade, Long entidadeId, String detalhes) {
        String usuarioEmail = obterEmailUsuarioAtual();

        AuditLog entrada = AuditLog.builder()
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .usuarioEmail(usuarioEmail)
                .detalhes(detalhes)
                .build();

        auditLogRepository.save(entrada);
        log.debug("Audit: {} {} id={} by {} - {}", acao, entidade, entidadeId, usuarioEmail, detalhes);
    }

    /**
     * Registra uma acao sem entidade especifica (ex: login).
     */
    @Transactional
    public void registrar(String acao, String detalhes) {
        String usuarioEmail = obterEmailUsuarioAtual();

        AuditLog entrada = AuditLog.builder()
                .acao(acao)
                .entidade("SISTEMA")
                .usuarioEmail(usuarioEmail)
                .detalhes(detalhes)
                .build();

        auditLogRepository.save(entrada);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> ultimosRegistros() {
        return auditLogRepository.findTop20ByOrderByCriadoEmDesc();
    }

    @Transactional(readOnly = true)
    public List<AuditLog> registrosDaEntidade(String entidade, Long entidadeId) {
        return auditLogRepository.findByEntidadeAndEntidadeIdOrderByCriadoEmDesc(entidade, entidadeId);
    }

    private String obterEmailUsuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null) {
            return auth.getName();
        }
        return "sistema";
    }
}
