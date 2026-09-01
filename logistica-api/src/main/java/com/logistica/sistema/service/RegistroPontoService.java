package com.logistica.sistema.service;

import com.logistica.sistema.dto.RegistroPontoResponse;
import com.logistica.sistema.exception.RegraNegocioException;
import com.logistica.sistema.model.RegistroPonto;
import com.logistica.sistema.model.Usuario;
import com.logistica.sistema.repository.RegistroPontoRepository;
import com.logistica.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistroPontoService {

    private final RegistroPontoRepository registroPontoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Registra a entrada do dia. O ponto e batido individualmente pelo
     * proprio funcionario (usuarioId vem sempre do usuario autenticado,
     * nunca do corpo da requisicao - diferente da movimentacao de
     * ativos, aqui nao faz sentido bater ponto "em nome" de outro).
     *
     * A confirmacao (equivalente a uma assinatura previamente
     * preenchida) acontece no proprio ato de bater o ponto - sem
     * geolocalizacao ou foto por enquanto, conforme decidido.
     */
    @Transactional
    public RegistroPontoResponse registrarEntrada(Long usuarioId) {
        LocalDate hoje = LocalDate.now();
        Usuario usuario = buscarUsuario(usuarioId);

        RegistroPonto registro = registroPontoRepository
                .findByUsuarioIdAndData(usuarioId, hoje)
                .orElse(null);

        if (registro != null && registro.getHoraEntrada() != null) {
            throw new RegraNegocioException("Você já registrou entrada hoje.");
        }

        if (registro == null) {
            registro = RegistroPonto.builder()
                    .usuario(usuario)
                    .data(hoje)
                    .build();
        }
        registro.setHoraEntrada(LocalTime.now());
        registro.setConfirmado(true);

        return RegistroPontoResponse.from(registroPontoRepository.save(registro));
    }

    @Transactional
    public RegistroPontoResponse registrarSaidaIntervalo(Long usuarioId) {
        RegistroPonto registro = buscarRegistroDeHoje(usuarioId);
        if (registro.getHoraEntrada() == null) {
            throw new RegraNegocioException("Registre a entrada antes de sair para o intervalo.");
        }
        if (registro.getHoraSaidaIntervalo() != null) {
            throw new RegraNegocioException("Você já registrou saída para o intervalo hoje.");
        }
        registro.setHoraSaidaIntervalo(LocalTime.now());
        return RegistroPontoResponse.from(registroPontoRepository.save(registro));
    }

    @Transactional
    public RegistroPontoResponse registrarVoltaIntervalo(Long usuarioId) {
        RegistroPonto registro = buscarRegistroDeHoje(usuarioId);
        if (registro.getHoraSaidaIntervalo() == null) {
            throw new RegraNegocioException("Registre a saída para o intervalo antes de registrar a volta.");
        }
        if (registro.getHoraVoltaIntervalo() != null) {
            throw new RegraNegocioException("Você já registrou a volta do intervalo hoje.");
        }
        registro.setHoraVoltaIntervalo(LocalTime.now());
        return RegistroPontoResponse.from(registroPontoRepository.save(registro));
    }

    @Transactional
    public RegistroPontoResponse registrarSaida(Long usuarioId) {
        RegistroPonto registro = buscarRegistroDeHoje(usuarioId);

        if (registro.getHoraEntrada() == null) {
            throw new RegraNegocioException("Registre a entrada antes de registrar a saída.");
        }
        if (registro.getHoraSaida() != null) {
            throw new RegraNegocioException("Você já registrou saída hoje.");
        }

        registro.setHoraSaida(LocalTime.now());
        return RegistroPontoResponse.from(registroPontoRepository.save(registro));
    }

    private RegistroPonto buscarRegistroDeHoje(Long usuarioId) {
        return registroPontoRepository.findByUsuarioIdAndData(usuarioId, LocalDate.now())
                .orElseThrow(() -> new RegraNegocioException(
                        "Registre a entrada antes de continuar."));
    }

    @Transactional(readOnly = true)
    public List<RegistroPontoResponse> listarPorPeriodo(
            Long usuarioId, LocalDate inicio, LocalDate fim) {
        return registroPontoRepository
                .findByUsuarioIdAndDataBetweenOrderByDataAsc(usuarioId, inicio, fim)
                .stream()
                .map(RegistroPontoResponse::from)
                .toList();
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RegraNegocioException(
                        "Usuário autenticado não encontrado (usuarioId=" + usuarioId + ")."));
    }
}
