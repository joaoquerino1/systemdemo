package com.logistica.sistema.service;

import com.logistica.sistema.dto.*;
import com.logistica.sistema.exception.RecursoNaoEncontradoException;
import com.logistica.sistema.exception.RegraNegocioException;
import com.logistica.sistema.model.*;
import com.logistica.sistema.repository.AtivoEpiRepository;
import com.logistica.sistema.repository.AtivoRepository;
import com.logistica.sistema.repository.AtivoVeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtivoService {

    private final AtivoRepository ativoRepository;
    private final AtivoVeiculoRepository ativoVeiculoRepository;
    private final AtivoEpiRepository ativoEpiRepository;
    private final AuditLogService auditLogService;

    /**
     * Cria um ativo. Se tipo == VEICULO, os dados de veiculo sao
     * obrigatorios (nao da pra ter um veiculo sem placa/modelo).
     * Se tipo == EPI, os dados de EPI sao opcionais (pode-se cadastrar
     * o numero do CA depois).
     */
    @Transactional
    public AtivoResponse criar(CriarAtivoRequest request) {
        if (ativoRepository.existsByCodigo(request.codigo())) {
            throw new RegraNegocioException(
                    "Ja existe um ativo com este codigo: " + request.codigo());
        }
        if (request.tipo() == TipoAtivo.VEICULO && request.veiculo() == null) {
            throw new RegraNegocioException(
                    "Dados do veiculo (placa, modelo) sao obrigatorios para ativos do tipo VEICULO.");
        }
        if (request.tipo() == TipoAtivo.VEICULO
                && ativoVeiculoRepository.findByPlaca(request.veiculo().placa()).isPresent()) {
            throw new RegraNegocioException(
                    "Ja existe um veiculo cadastrado com esta placa: " + request.veiculo().placa());
        }

        Ativo ativo = Ativo.builder()
                .tipo(request.tipo())
                .nome(request.nome())
                .codigo(request.codigo())
                .build();
        ativo = ativoRepository.save(ativo);

        DadosVeiculoResponse veiculoResponse = null;
        DadosEpiResponse epiResponse = null;

        if (request.tipo() == TipoAtivo.VEICULO) {
            AtivoVeiculo av = criarExtensaoVeiculo(ativo, request.veiculo());
            veiculoResponse = DadosVeiculoResponse.from(av);
        } else if (request.tipo() == TipoAtivo.EPI && request.epi() != null) {
            AtivoEpi ae = criarExtensaoEpi(ativo, request.epi());
            epiResponse = DadosEpiResponse.from(ae);
        }

        // Audit log
        auditLogService.registrar(
                "CRIACAO",
                "ATIVO",
                ativo.getId(),
                String.format("Ativo: %s (%s) tipo=%s",
                        ativo.getNome(), ativo.getCodigo(), ativo.getTipo())
        );

        return AtivoResponse.from(ativo, veiculoResponse, epiResponse);
    }

    /**
     * Lista ativos com paginacao e filtros opcionais.
     * Carrega os subtipos (veiculo/epi) em lote para evitar N+1:
     * 1 query para ativos + 1 query por tipo de subtipo presente
     * na pagina, no maximo 2 queries extras no total.
     */
    @Transactional(readOnly = true)
    public Page<AtivoResponse> listar(TipoAtivo tipo, StatusAtivo status, Pageable pageable) {
        Page<Ativo> pagina = ativoRepository.buscarComFiltros(tipo, status, pageable);

        List<Long> ids = pagina.getContent().stream().map(Ativo::getId).toList();

        // Batch-load subtipos em 2 queries (uma por tipo presente)
        Map<Long, AtivoVeiculo> veiculosMap = ativoVeiculoRepository.findByAtivoIdIn(ids)
                .stream().collect(Collectors.toMap(AtivoVeiculo::getAtivoId, v -> v));
        Map<Long, AtivoEpi> episMap = ativoEpiRepository.findByAtivoIdIn(ids)
                .stream().collect(Collectors.toMap(AtivoEpi::getAtivoId, e -> e));

        return pagina.map(a -> {
            DadosVeiculoResponse veiculo = Optional.ofNullable(veiculosMap.get(a.getId()))
                    .map(DadosVeiculoResponse::from).orElse(null);
            DadosEpiResponse epi = Optional.ofNullable(episMap.get(a.getId()))
                    .map(DadosEpiResponse::from).orElse(null);
            return AtivoResponse.from(a, veiculo, epi);
        });
    }

    @Transactional(readOnly = true)
    public AtivoResponse buscarPorCodigo(String codigo) {
        Ativo ativo = buscarAtivoOuFalhar(codigo);
        return montarResponse(ativo);
    }

    @Transactional
    public AtivoResponse atualizar(String codigo, AtualizarAtivoRequest request) {
        Ativo ativo = buscarAtivoOuFalhar(codigo);

        // nao deixa "destrancar" manualmente um ativo que esta EM_USO -
        // isso teria que passar pelo fluxo de devolucao, senao o status
        // do ativo fica dessincronizado da movimentacao aberta
        if (ativo.getStatus() == StatusAtivo.EM_USO && request.status() != StatusAtivo.EM_USO) {
            throw new RegraNegocioException(
                    "Este ativo esta em uso. Registre a devolucao antes de alterar o status.");
        }

        ativo.setNome(request.nome());
        ativo.setStatus(request.status());
        ativo = ativoRepository.save(ativo);

        DadosVeiculoResponse veiculoResponse = null;
        DadosEpiResponse epiResponse = null;

        if (ativo.getTipo() == TipoAtivo.VEICULO && request.veiculo() != null) {
            AtivoVeiculo av = ativoVeiculoRepository.findById(ativo.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Dados de veiculo nao encontrados para este ativo."));
            av.setMarca(request.veiculo().marca());
            av.setModelo(request.veiculo().modelo());
            if (request.veiculo().kmAtual() != null) {
                av.setKmAtual(request.veiculo().kmAtual());
            }
            av.setDataUltimaManutencao(request.veiculo().dataUltimaManutencao());
            // placa nao e atualizada aqui de proposito - mesma logica do codigo
            veiculoResponse = DadosVeiculoResponse.from(ativoVeiculoRepository.save(av));
        } else if (ativo.getTipo() == TipoAtivo.VEICULO) {
            veiculoResponse = ativoVeiculoRepository.findById(ativo.getId())
                    .map(DadosVeiculoResponse::from).orElse(null);
        }

        if (ativo.getTipo() == TipoAtivo.EPI && request.epi() != null) {
            Ativo ativoFinal = ativo;
            AtivoEpi ae = ativoEpiRepository.findById(ativo.getId())
                    .orElseGet(() -> AtivoEpi.builder().ativo(ativoFinal).build());
            ae.setNumeroCa(request.epi().numeroCa());
            ae.setValidadeCa(request.epi().validadeCa());
            epiResponse = DadosEpiResponse.from(ativoEpiRepository.save(ae));
        } else if (ativo.getTipo() == TipoAtivo.EPI) {
            epiResponse = ativoEpiRepository.findById(ativo.getId())
                    .map(DadosEpiResponse::from).orElse(null);
        }

        return AtivoResponse.from(ativo, veiculoResponse, epiResponse);
    }

    /**
     * Desativa um ativo (soft delete - o registro nunca e excluido de
     * verdade, pra preservar o historico de movimentacoes). Um ativo
     * EM_USO nao pode ser desativado sem devolucao antes.
     */
    @Transactional
    public void desativar(String codigo) {
        Ativo ativo = buscarAtivoOuFalhar(codigo);
        if (ativo.getStatus() == StatusAtivo.EM_USO) {
            throw new RegraNegocioException(
                    "Este ativo esta em uso. Registre a devolucao antes de desativar.");
        }
        ativo.setAtivo(false);
        ativo.setStatus(StatusAtivo.INATIVO);
        ativoRepository.save(ativo);

        // Audit log
        auditLogService.registrar(
                "DESATIVACAO",
                "ATIVO",
                ativo.getId(),
                String.format("Ativo: %s (%s)",
                        ativo.getNome(), ativo.getCodigo())
        );
    }

    /**
     * Estatisticas para o dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // Contagem por status
        Map<String, Long> porStatus = new LinkedHashMap<>();
        for (Object[] row : ativoRepository.contarPorStatus()) {
            porStatus.put(((StatusAtivo) row[0]).name(), (Long) row[1]);
        }
        stats.put("porStatus", porStatus);

        // Contagem por tipo
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (Object[] row : ativoRepository.contarPorTipo()) {
            porTipo.put(((TipoAtivo) row[0]).name(), (Long) row[1]);
        }
        stats.put("porTipo", porTipo);

        return stats;
    }

    private Ativo buscarAtivoOuFalhar(String codigo) {
        return ativoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ativo nao encontrado: " + codigo));
    }

    private AtivoVeiculo criarExtensaoVeiculo(Ativo ativo, DadosVeiculoRequest dados) {
        AtivoVeiculo av = AtivoVeiculo.builder()
                .ativo(ativo)
                .placa(dados.placa())
                .marca(dados.marca())
                .modelo(dados.modelo())
                .kmAtual(dados.kmAtual() != null ? dados.kmAtual() : 0)
                .dataUltimaManutencao(dados.dataUltimaManutencao())
                .build();
        return ativoVeiculoRepository.save(av);
    }

    private AtivoEpi criarExtensaoEpi(Ativo ativo, DadosEpiRequest dados) {
        AtivoEpi ae = AtivoEpi.builder()
                .ativo(ativo)
                .numeroCa(dados.numeroCa())
                .validadeCa(dados.validadeCa())
                .build();
        return ativoEpiRepository.save(ae);
    }

    /**
     * Monta response com fetch dos dados de extensao (veiculo/epi).
     * Fix do N+1: em vez de fazer query para cada ativo na listagem,
     * usamos queries em lote quando necessario.
     */
    private AtivoResponse montarResponse(Ativo ativo) {
        DadosVeiculoResponse veiculo = null;
        DadosEpiResponse epi = null;

        if (ativo.getTipo() == TipoAtivo.VEICULO) {
            veiculo = ativoVeiculoRepository.findById(ativo.getId())
                    .map(DadosVeiculoResponse::from).orElse(null);
        } else if (ativo.getTipo() == TipoAtivo.EPI) {
            epi = ativoEpiRepository.findById(ativo.getId())
                    .map(DadosEpiResponse::from).orElse(null);
        }

        return AtivoResponse.from(ativo, veiculo, epi);
    }
}
