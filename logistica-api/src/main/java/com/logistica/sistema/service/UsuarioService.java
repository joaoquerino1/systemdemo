package com.logistica.sistema.service;

import com.logistica.sistema.dto.CriarUsuarioRequest;
import com.logistica.sistema.dto.UsuarioResponse;
import com.logistica.sistema.exception.RegraNegocioException;
import com.logistica.sistema.model.Role;
import com.logistica.sistema.model.Usuario;
import com.logistica.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(UsuarioResponse::from);
    }

    /**
     * Cria o primeiro usuario ADMIN do sistema. So funciona enquanto
     * nao existir nenhum usuario cadastrado - resolve o problema de
     * "ovo e galinha" (nao da pra exigir um ADMIN logado pra criar
     * o primeiro ADMIN). Depois que o primeiro usuario existir, este
     * endpoint passa a rejeitar qualquer chamada.
     */
    @Transactional
    public UsuarioResponse criarPrimeiroAdmin(CriarUsuarioRequest request) {
        if (usuarioRepository.count() > 0) {
            throw new RegraNegocioException(
                    "Ja existe pelo menos um usuario cadastrado. " +
                    "Peca para um administrador criar sua conta.");
        }
        return salvar(request, Role.ADMIN);
    }

    /**
     * Cria um usuario comum. Protegido no SecurityConfig/controller
     * para que so um ADMIN autenticado possa chamar.
     */
    @Transactional
    public UsuarioResponse criarUsuario(CriarUsuarioRequest request) {
        Role role = request.role() != null ? request.role() : Role.FUNCIONARIO;
        return salvar(request, role);
    }

    /**
     * Estatisticas para o dashboard.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsuarios", usuarioRepository.count());
        stats.put("usuariosAtivos", usuarioRepository.countByAtivoTrue());
        return stats;
    }

    private UsuarioResponse salvar(CriarUsuarioRequest request, Role role) {
        String emailNormalizado = request.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new RegraNegocioException("Ja existe um usuario com este email.");
        }
        if (usuarioRepository.existsByCpf(request.cpf())) {
            throw new RegraNegocioException("Ja existe um usuario com este CPF.");
        }
        if (usuarioRepository.existsByMatricula(request.matricula())) {
            throw new RegraNegocioException("Ja existe um usuario com esta matricula.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .cpf(request.cpf())
                .matricula(request.matricula())
                .cargo(request.cargo())
                .setor(request.setor())
                .email(emailNormalizado)
                .senhaHash(passwordEncoder.encode(request.senha()))
                .role(role)
                .build();

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }
}
