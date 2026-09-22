package com.logistica.sistema.service;

import com.logistica.sistema.dto.*;
import com.logistica.sistema.model.*;
import com.logistica.sistema.repository.CronogramaEtapaRepository;
import com.logistica.sistema.repository.OrdemServicoRepository;
import com.logistica.sistema.repository.SerialProducaoRepository;
import com.logistica.sistema.repository.UsuarioRepository;
import com.logistica.sistema.exception.RegraNegocioException;
import com.logistica.sistema.exception.RecursoNaoEncontradoException;
import com.logistica.sistema.util.HorarioBrasil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Regras do cronograma de producao (doc docs/plano-cronograma-producao.md):
 *
 * - Ordem das etapas e LIVRE: qualquer etapa pode iniciar a qualquer
 *   momento; a unica restricao de sequencia e a CONFERENCIA, que so
 *   conclui com as outras 6 concluidas ("conferir o material produzido
 *   ao concluir a serial", rodape do formulario);
 * - Concluir exige data de inicio registrada e quantidade > 0;
 * - Reabrir etapa concluida e acao administrativa (ADMIN/GESTOR),
 *   sempre auditada.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProducaoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final SerialProducaoRepository serialProducaoRepository;
    private final CronogramaEtapaRepository cronogramaEtapaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditLogService auditLogService;

    // ------------------------------------------------------------------
    // OS
    // ------------------------------------------------------------------

    @Transactional
    public OrdemServicoResponse criarOS(CriarOrdemServicoRequest request) {
        if (request.numero() == null || request.numero().isBlank()) {
            throw new RegraNegocioException("O numero da OS e obrigatorio.");
        }
        if (request.seriais() == null || request.seriais().isEmpty()) {
            throw new RegraNegocioException(
                    "Cadastre ao menos um serial para abrir a OS.");
        }
        if (ordemServicoRepository.existsByNumero(request.numero().trim())) {
            throw new RegraNegocioException(
                    "Ja existe uma OS com este numero: " + request.numero().trim());
        }

        List<String> codigos = request.seriais().stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .toList();
        if (codigos.isEmpty()) {
            throw new RegraNegocioException(
                    "Cadastre ao menos um serial valido para abrir a OS.");
        }
        Set<String> unicos = new HashSet<>(codigos);
        if (unicos.size() != codigos.size()) {
            throw new RegraNegocioException(
                    "Ha codigos de serial repetidos na lista da OS.");
        }

        OrdemServico os = ordemServicoRepository.save(OrdemServico.builder()
                .numero(request.numero().trim())
                .descricao(request.descricao())
                .build());

        for (String codigo : codigos) {
            criarSerialInterno(os, codigo);
        }

        auditLogService.registrar("CRIACAO", "OS", os.getId(),
                String.format("OS %s com %d serial(is)", os.getNumero(), codigos.size()));

        log.info("OS {} criada com {} serial(is)", os.getNumero(), codigos.size());
        return OrdemServicoResponse.resumo(os);
    }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResponse> listarOS(StatusOS status, String busca, Pageable pageable) {
        String termo = (busca != null && !busca.isBlank()) ? busca.trim() : null;

        Page<OrdemServico> pagina;
        if (termo != null && status != null) {
            pagina = ordemServicoRepository.buscarPorStatusETermo(status, termo, pageable);
        } else if (termo != null) {
            pagina = ordemServicoRepository.buscarPorTermo(termo, pageable);
        } else if (status != null) {
            pagina = ordemServicoRepository.findByStatus(status, pageable);
        } else {
            pagina = ordemServicoRepository.findAll(pageable);
        }

        if (pagina.isEmpty()) {
            return pagina.map(OrdemServicoResponse::resumo);
        }

        // 1 query agregada para trazer total/concluidos de todas as OS
        // da pagina (em vez de 2 counts por OS - N+1).
        List<Long> ids = pagina.getContent().stream().map(OrdemServico::getId).toList();
        Map<Long, long[]> progressoPorOS = new HashMap<>();
        for (Object[] linha : serialProducaoRepository.contarSeriaisPorOS(ids)) {
            Long osId = (Long) linha[0];
            StatusSerial statusSerial = (StatusSerial) linha[1];
            long contagem = (Long) linha[2];
            long[] progresso = progressoPorOS.computeIfAbsent(osId, k -> new long[]{0, 0});
            progresso[0] += contagem;
            if (statusSerial == StatusSerial.CONCLUIDO) {
                progresso[1] += contagem;
            }
        }

        return pagina.map(os -> {
            long[] progresso = progressoPorOS.getOrDefault(os.getId(), new long[]{0, 0});
            return OrdemServicoResponse.detalhado(os, null, progresso[0], progresso[1]);
        });
    }

    /**
     * Detalhe completo da OS: seriais + as 7 etapas de cada um,
     * carregadas em lote (1 query seriais + 1 query etapas).
     */
    @Transactional(readOnly = true)
    public OrdemServicoResponse detalharOS(Long osId) {
        OrdemServico os = buscarOS(osId);
        List<SerialProducao> seriais =
                serialProducaoRepository.findByOrdemServicoId(osId);

        Map<Long, List<CronogramaEtapa>> etapasPorSerial =
                cronogramaEtapaRepository.findBySerialIdInOrderByIdAsc(
                                seriais.stream().map(SerialProducao::getId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getSerial().getId(),
                                LinkedHashMap::new,
                                Collectors.toList()));

        List<SerialProducaoResponse> seriaisResponse = seriais.stream()
                .map(s -> SerialProducaoResponse.from(s,
                        etapasPorSerial
                                .getOrDefault(s.getId(), List.of())
                                .stream()
                                .map(EtapaResponse::from)
                                .toList()))
                .toList();

        long concluidos = seriais.stream()
                .filter(s -> s.getStatus() == StatusSerial.CONCLUIDO)
                .count();

        return OrdemServicoResponse.detalhado(os, seriaisResponse, seriais.size(), concluidos);
    }

    @Transactional
    public SerialProducaoResponse adicionarSerial(Long osId, String codigoSerial) {
        OrdemServico os = buscarOS(osId);
        if (os.getStatus() == StatusOS.CONCLUIDA || os.getStatus() == StatusOS.CANCELADA) {
            throw new RegraNegocioException(
                    "Nao e possivel adicionar serial a uma OS " + os.getStatus() + ".");
        }
        String codigo = codigoSerial == null ? "" : codigoSerial.trim();
        if (codigo.isEmpty()) {
            throw new RegraNegocioException("O codigo do serial e obrigatorio.");
        }
        if (serialProducaoRepository.existsByOrdemServicoIdAndCodigoSerial(osId, codigo)) {
            throw new RegraNegocioException(
                    "Ja existe este serial nesta OS: " + codigo);
        }

        SerialProducao serial = criarSerialInterno(os, codigo);

        auditLogService.registrar("CRIACAO", "SERIAL", serial.getId(),
                String.format("Serial %s adicionado a OS %s", codigo, os.getNumero()));

        return SerialProducaoResponse.from(serial, etapasDoSerial(serial.getId()));
    }

    // ------------------------------------------------------------------
    // Etapas
    // ------------------------------------------------------------------

    /**
     * Inicia uma etapa. O colaborador pode ser informado (por
     * ADMIN/GESTOR, via controller) ou assume o usuario autenticado.
     */
    @Transactional
    public EtapaResponse iniciarEtapa(Long etapaId, Long colaboradorId) {
        CronogramaEtapa etapa = buscarEtapa(etapaId);
        validarOSAtiva(etapa);

        if (etapa.getStatus() != StatusEtapa.PENDENTE) {
            throw new RegraNegocioException(
                    "So e possivel iniciar uma etapa PENDENTE (atual: "
                            + etapa.getStatus() + ").");
        }

        Usuario colaborador = buscarUsuario(colaboradorId);

        etapa.setColaborador(colaborador);
        etapa.setDataInicio(HorarioBrasil.agoraCompleta());
        etapa.setStatus(StatusEtapa.EM_ANDAMENTO);

        CronogramaEtapa salva = cronogramaEtapaRepository.save(etapa);

        // Ao iniciar qualquer etapa, o serial e a OS saem do estado
        // inicial "parado" (nao afeta os que ja estao em producao).
        promoverSerialEOS(etapa.getSerial());

        auditLogService.registrar("INICIO_ETAPA", "ETAPA_PRODUCAO", salva.getId(),
                String.format("Etapa %s do serial %s iniciada por %s",
                        etapa.getEtapa(), etapa.getSerial().getCodigoSerial(),
                        colaborador.getNome()));

        return EtapaResponse.from(salva);
    }

    /**
     * Conclui uma etapa. Exige data de inicio registrada e quantidade
     * > 0 (decisao confirmada). CONFERENCIA so conclui com as outras
     * 6 etapas do serial ja concluidas.
     */
    @Transactional
    public EtapaResponse concluirEtapa(Long etapaId, ConcluirEtapaRequest request) {
        CronogramaEtapa etapa = buscarEtapa(etapaId);
        validarOSAtiva(etapa);

        if (etapa.getStatus() == StatusEtapa.PENDENTE) {
            throw new RegraNegocioException(
                    "Inicie a etapa antes de conclui-la.");
        }
        if (etapa.getStatus() == StatusEtapa.CONCLUIDA) {
            throw new RegraNegocioException(
                    "Esta etapa ja esta concluida.");
        }
        if (etapa.getDataInicio() == null) {
            throw new RegraNegocioException(
                    "Etapa sem data de inicio registrada.");
        }
        if (request.quantidadeProduzida() == null || request.quantidadeProduzida() <= 0) {
            throw new RegraNegocioException(
                    "A quantidade produzida e obrigatoria e deve ser maior que zero.");
        }

        if (etapa.getEtapa() == EtapaProducao.CONFERENCIA) {
            long outrasConcluidas = cronogramaEtapaRepository
                    .countBySerialIdAndStatus(etapa.getSerial().getId(), StatusEtapa.CONCLUIDA);
            long faltando = 6 - outrasConcluidas;
            if (faltando > 0) {
                throw new RegraNegocioException(
                        "A CONFERENCIA e a ultima etapa: faltam " + faltando
                                + " etapa(s) a concluir antes dela.");
            }
        }

        etapa.setQuantidadeProduzida(request.quantidadeProduzida());
        etapa.setPendencias(request.pendencias());
        etapa.setDataConclusao(HorarioBrasil.agoraCompleta());
        etapa.setStatus(StatusEtapa.CONCLUIDA);

        CronogramaEtapa salva = cronogramaEtapaRepository.save(etapa);

        SerialProducao serial = etapa.getSerial();
        long concluidas = cronogramaEtapaRepository
                .countBySerialIdAndStatus(serial.getId(), StatusEtapa.CONCLUIDA);

        if (concluidas == 7) {
            serial.setStatus(StatusSerial.CONCLUIDO);
            serialProducaoRepository.save(serial);
            auditLogService.registrar("CONCLUSAO_SERIAL", "SERIAL", serial.getId(),
                    String.format("Serial %s da OS %s concluido (todas as 7 etapas)",
                            serial.getCodigoSerial(), serial.getOrdemServico().getNumero()));
        } else {
            promoverSerialEOS(serial);
        }

        // OS concluida quando todos os seriais estao concluidos
        OrdemServico os = serial.getOrdemServico();
        if (os.getStatus() != StatusOS.CONCLUIDA) {
            long seriaisConcluidos = serialProducaoRepository
                    .countByOrdemServicoIdAndStatus(os.getId(), StatusSerial.CONCLUIDO);
            long totalSeriais = serialProducaoRepository.findByOrdemServicoId(os.getId()).size();
            if (totalSeriais > 0 && seriaisConcluidos == totalSeriais) {
                os.setStatus(StatusOS.CONCLUIDA);
                ordemServicoRepository.save(os);
                auditLogService.registrar("CONCLUSAO_OS", "OS", os.getId(),
                        String.format("OS %s concluida (todos os seriais concluidos)",
                                os.getNumero()));
            }
        }

        auditLogService.registrar("CONCLUSAO_ETAPA", "ETAPA_PRODUCAO", salva.getId(),
                String.format("Etapa %s do serial %s concluida (qtd=%d)",
                        etapa.getEtapa(), serial.getCodigoSerial(),
                        request.quantidadeProduzida()));

        return EtapaResponse.from(salva);
    }

    /**
     * Registra/atualiza pendencias de uma etapa em andamento, sem
     * conclui-la.
     */
    @Transactional
    public EtapaResponse registrarPendencia(Long etapaId, String texto) {
        CronogramaEtapa etapa = buscarEtapa(etapaId);
        validarOSAtiva(etapa);

        if (etapa.getStatus() == StatusEtapa.PENDENTE) {
            throw new RegraNegocioException(
                    "Inicie a etapa antes de registrar pendencias.");
        }
        if (etapa.getStatus() == StatusEtapa.CONCLUIDA) {
            throw new RegraNegocioException(
                    "Nao e possivel registrar pendencia em etapa concluida. Reabra a etapa (ADMIN/GESTOR).");
        }
        if (texto == null || texto.isBlank()) {
            throw new RegraNegocioException("O texto da pendencia e obrigatorio.");
        }

        etapa.setPendencias(texto.trim());
        return EtapaResponse.from(cronogramaEtapaRepository.save(etapa));
    }

    /**
     * Reabre uma etapa concluida. Exclusivo de ADMIN/GESTOR (checado
     * no controller) e sempre auditado com o motivo informado.
     */
    @Transactional
    public EtapaResponse reabrirEtapa(Long etapaId, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException(
                    "O motivo da reabertura e obrigatorio (registro de auditoria).");
        }
        CronogramaEtapa etapa = buscarEtapa(etapaId);
        validarOSAtiva(etapa);

        if (etapa.getStatus() != StatusEtapa.CONCLUIDA) {
            throw new RegraNegocioException(
                    "So e possivel reabrir uma etapa CONCLUIDA (atual: "
                            + etapa.getStatus() + ").");
        }

        etapa.setStatus(StatusEtapa.EM_ANDAMENTO);
        etapa.setDataConclusao(null);
        // quantidade fica como historico da conclusao anterior; sera
        // substituida na nova conclusao.

        // Se o serial tinha sido concluido pela CONFERENCIA, volta
        // para producao; o mesmo para a OS.
        SerialProducao serial = etapa.getSerial();
        if (serial.getStatus() == StatusSerial.CONCLUIDO) {
            serial.setStatus(StatusSerial.EM_PRODUCAO);
            serialProducaoRepository.save(serial);
        }
        OrdemServico os = serial.getOrdemServico();
        if (os.getStatus() == StatusOS.CONCLUIDA) {
            os.setStatus(StatusOS.EM_PRODUCAO);
            ordemServicoRepository.save(os);
        }

        auditLogService.registrar("REABERTURA_ETAPA", "ETAPA_PRODUCAO", etapa.getId(),
                String.format("Etapa %s do serial %s reaberta. Motivo: %s",
                        etapa.getEtapa(), serial.getCodigoSerial(), motivo.trim()));

        return EtapaResponse.from(cronogramaEtapaRepository.save(etapa));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Cria o serial ja com as 7 etapas PENDENTE (linhas do formulario). */
    private SerialProducao criarSerialInterno(OrdemServico os, String codigo) {
        SerialProducao serial = serialProducaoRepository.save(SerialProducao.builder()
                .ordemServico(os)
                .codigoSerial(codigo)
                .build());

        List<CronogramaEtapa> etapas = new ArrayList<>(EtapaProducao.values().length);
        for (EtapaProducao etapa : EtapaProducao.values()) {
            etapas.add(CronogramaEtapa.builder()
                    .serial(serial)
                    .etapa(etapa)
                    .build());
        }
        cronogramaEtapaRepository.saveAll(etapas);
        return serial;
    }

    /**
     * Serial recem-iniciado vai a EM_PRODUCAO (se pendente) e a OS
     * acompanha (se ABERTA). Nao rebaixa status ja avancados.
     */
    private void promoverSerialEOS(SerialProducao serial) {
        if (serial.getStatus() == StatusSerial.PENDENTE) {
            serial.setStatus(StatusSerial.EM_PRODUCAO);
            serialProducaoRepository.save(serial);
        }
        OrdemServico os = serial.getOrdemServico();
        if (os.getStatus() == StatusOS.ABERTA) {
            os.setStatus(StatusOS.EM_PRODUCAO);
            ordemServicoRepository.save(os);
        }
    }

    private List<EtapaResponse> etapasDoSerial(Long serialId) {
        return cronogramaEtapaRepository.findBySerialIdOrderByIdAsc(serialId)
                .stream()
                .map(EtapaResponse::from)
                .toList();
    }

    private void validarOSAtiva(CronogramaEtapa etapa) {
        StatusOS statusOS = etapa.getSerial().getOrdemServico().getStatus();
        if (statusOS == StatusOS.CONCLUIDA || statusOS == StatusOS.CANCELADA) {
            throw new RegraNegocioException(
                    "Esta OS esta " + statusOS + "; suas etapas nao podem ser alteradas.");
        }
    }

    private OrdemServico buscarOS(Long id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "OS nao encontrada: id=" + id));
    }

    private CronogramaEtapa buscarEtapa(Long id) {
        return cronogramaEtapaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Etapa nao encontrada: id=" + id));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuario nao encontrado: id=" + id));
    }
}
