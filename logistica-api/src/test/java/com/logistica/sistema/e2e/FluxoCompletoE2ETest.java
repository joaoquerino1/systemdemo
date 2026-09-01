package com.logistica.sistema.e2e;

import com.jayway.jsonpath.JsonPath;
import com.logistica.sistema.BaseIntegrationTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes end-to-end do fluxo completo do sistema, na ordem em que
 * aconteceriam no uso real: bootstrap do admin -> login -> criacao de
 * funcionario -> cadastro de ativo -> retirada/devolucao -> ponto ->
 * folha de hora em PDF. Os metodos sao ordenados (@Order) porque cada
 * etapa depende do estado deixado pela anterior - nao sao testes
 * unitarios isolados, e sim uma simulacao de sessao real de uso.
 *
 * Sobe um PostgreSQL real via Testcontainers (ver BaseIntegrationTest)
 * - por isso e necessario ter o Docker rodando na maquina que executar
 * `mvn test`.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FluxoCompletoE2ETest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String tokenAdmin;
    private static String tokenFuncionario;
    private static Long funcionarioId;
    private static String codigoVeiculo = "ABC1D23";

    // ---------------------------------------------------------------
    // Bootstrap do admin + login
    // ---------------------------------------------------------------

    @Test
    @Order(1)
    void bootstrapAdmin_primeiraChamada_deveCriarAdminERetornar201() throws Exception {
        String corpo = """
                {
                  "nome": "Admin Geral",
                  "cpf": "11111111111",
                  "matricula": "0001",
                  "email": "admin@empresa.com",
                  "senha": "senha12345"
                }
                """;

        mockMvc.perform(post("/api/usuarios/bootstrap-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("admin@empresa.com"));
    }

    @Test
    @Order(2)
    void bootstrapAdmin_segundaChamada_deveFalharComConflito() throws Exception {
        // ja existe um usuario (o admin criado no teste anterior) -
        // este endpoint so pode ser usado uma unica vez no sistema
        String corpo = """
                {
                  "nome": "Outro Admin",
                  "cpf": "22222222222",
                  "matricula": "0002",
                  "email": "outro@empresa.com",
                  "senha": "senha12345"
                }
                """;

        mockMvc.perform(post("/api/usuarios/bootstrap-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    void login_comEmailEmMaiusculo_deveFuncionarMesmoAssim() throws Exception {
        // valida a normalizacao de email (case-insensitive) - o
        // cadastro foi feito com email em minusculo, aqui logamos
        // usando maiusculo de proposito
        String corpo = """
                {"email": "ADMIN@EMPRESA.COM", "senha": "senha12345"}
                """;

        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        tokenAdmin = JsonPath.read(resultado.getResponse().getContentAsString(), "$.token");
        assertThat(tokenAdmin).isNotBlank();
    }

    @Test
    @Order(4)
    void login_comSenhaErrada_deveRetornar401ComMensagemGenerica() throws Exception {
        String corpo = """
                {"email": "admin@empresa.com", "senha": "senhaErrada"}
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    // ---------------------------------------------------------------
    // Criacao de funcionario (protegida por ADMIN)
    // ---------------------------------------------------------------

    @Test
    @Order(5)
    void criarUsuario_semToken_deveRetornar401() throws Exception {
        String corpo = """
                {
                  "nome": "Funcionario Teste",
                  "cpf": "33333333333",
                  "matricula": "0003",
                  "email": "funcionario@empresa.com",
                  "senha": "senha12345"
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    void criarUsuario_comTokenAdmin_deveCriarFuncionario() throws Exception {
        String corpo = """
                {
                  "nome": "Funcionario Teste",
                  "cpf": "33333333333",
                  "matricula": "0003",
                  "email": "funcionario@empresa.com",
                  "senha": "senha12345",
                  "role": "FUNCIONARIO"
                }
                """;

        MvcResult resultado = mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("FUNCIONARIO"))
                .andReturn();

        funcionarioId = JsonPath.parse(resultado.getResponse().getContentAsString()).read("$.id", Long.class);

        // login do funcionario recem-criado, pra usar nos proximos testes
        String loginCorpo = """
                {"email": "funcionario@empresa.com", "senha": "senha12345"}
                """;
        MvcResult loginResultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginCorpo))
                .andExpect(status().isOk())
                .andReturn();
        tokenFuncionario = JsonPath.read(loginResultado.getResponse().getContentAsString(), "$.token");
    }

    @Test
    @Order(7)
    void criarUsuario_comTokenDeFuncionario_deveRetornar403() throws Exception {
        String corpo = """
                {
                  "nome": "Outro Funcionario",
                  "cpf": "44444444444",
                  "matricula": "0004",
                  "email": "outro.funcionario@empresa.com",
                  "senha": "senha12345"
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------
    // CRUD de Ativos (com paginacao)
    // ---------------------------------------------------------------

    @Test
    @Order(8)
    void criarAtivoVeiculo_comoAdmin_deveFuncionar() throws Exception {
        String corpo = """
                {
                  "tipo": "VEICULO",
                  "nome": "Fiorino de entregas",
                  "codigo": "%s",
                  "veiculo": {
                    "placa": "%s",
                    "marca": "Fiat",
                    "modelo": "Fiorino",
                    "kmAtual": 15000
                  }
                }
                """.formatted(codigoVeiculo, codigoVeiculo);

        mockMvc.perform(post("/api/ativos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.veiculo.placa").value(codigoVeiculo));
    }

    @Test
    @Order(9)
    void criarAtivoVeiculo_comPlacaDuplicadaEmCodigoDiferente_deveRetornar409() throws Exception {
        // mesma placa do teste anterior, mas codigo diferente -
        // valida a correcao do bug de placa duplicada nao verificada
        String corpo = """
                {
                  "tipo": "VEICULO",
                  "nome": "Outro veiculo com placa repetida",
                  "codigo": "CODIGO-DIFERENTE-01",
                  "veiculo": {
                    "placa": "%s",
                    "marca": "Fiat",
                    "modelo": "Fiorino",
                    "kmAtual": 0
                  }
                }
                """.formatted(codigoVeiculo);

        mockMvc.perform(post("/api/ativos")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(10)
    void criarAtivo_comoFuncionario_deveRetornar403() throws Exception {
        String corpo = """
                {"tipo": "FERRAMENTA", "nome": "Furadeira", "codigo": "FERR-001"}
                """;

        mockMvc.perform(post("/api/ativos")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(11)
    void listarAtivos_comoFuncionario_deveFuncionar() throws Exception {
        // qualquer usuario autenticado pode consultar (nao so ADMIN/GESTOR)
        mockMvc.perform(get("/api/ativos")
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content[0].codigo").exists());
    }

    // ---------------------------------------------------------------
    // Retirada e devolucao (movimentacao)
    // ---------------------------------------------------------------

    @Test
    @Order(12)
    void retirarAtivo_primeiraVez_deveFuncionar() throws Exception {
        String corpo = """
                {"codigoAtivo": "%s", "usuarioId": %d, "kmSaida": 15010}
                """.formatted(codigoVeiculo, funcionarioId);

        mockMvc.perform(post("/api/movimentacoes/retirada")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    @Order(13)
    void retirarAtivo_jaEmUso_deveRetornar409() throws Exception {
        String corpo = """
                {"codigoAtivo": "%s", "usuarioId": %d}
                """.formatted(codigoVeiculo, funcionarioId);

        mockMvc.perform(post("/api/movimentacoes/retirada")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(14)
    void ativo_apos_retirada_deveEstarComStatusEmUso() throws Exception {
        mockMvc.perform(get("/api/ativos/" + codigoVeiculo)
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_USO"));
    }

    @Test
    @Order(15)
    void devolverAtivo_deveFuncionarEVoltarParaDisponivel() throws Exception {
        String corpo = """
                {"codigoAtivo": "%s", "kmChegada": 15080}
                """.formatted(codigoVeiculo);

        mockMvc.perform(post("/api/movimentacoes/devolucao")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADO"));

        mockMvc.perform(get("/api/ativos/" + codigoVeiculo)
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPONIVEL"))
                .andExpect(jsonPath("$.veiculo.kmAtual").value(15080));
    }

    @Test
    @Order(16)
    void retirarAtivo_novamenteAposDevolucao_deveFuncionar() throws Exception {
        // garante que devolver realmente libera o ativo pra uma nova retirada
        String corpo = """
                {"codigoAtivo": "%s", "usuarioId": %d}
                """.formatted(codigoVeiculo, funcionarioId);

        mockMvc.perform(post("/api/movimentacoes/retirada")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated());

        // devolve de novo pra deixar o ativo livre para os proximos testes
        String devolucao = """
                {"codigoAtivo": "%s"}
                """.formatted(codigoVeiculo);
        mockMvc.perform(post("/api/movimentacoes/devolucao")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(devolucao))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------------
    // Ponto (entrada, intervalo, saida)
    // ---------------------------------------------------------------

    @Test
    @Order(17)
    void baterPonto_entrada_deveFuncionar() throws Exception {
        mockMvc.perform(post("/api/ponto/entrada")
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaEntrada").exists())
                .andExpect(jsonPath("$.confirmado").value(true));
    }

    @Test
    @Order(18)
    void baterPonto_entradaDuplicadaNoMesmoDia_deveRetornar409() throws Exception {
        mockMvc.perform(post("/api/ponto/entrada")
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(19)
    void baterPonto_saidaIntervalo_deveFuncionar() throws Exception {
        mockMvc.perform(post("/api/ponto/intervalo/saida")
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaSaidaIntervalo").exists());
    }

    @Test
    @Order(20)
    void baterPonto_voltaIntervalo_deveFuncionar() throws Exception {
        mockMvc.perform(post("/api/ponto/intervalo/volta")
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaVoltaIntervalo").exists());
    }

    @Test
    @Order(21)
    void baterPonto_saida_deveFuncionar() throws Exception {
        mockMvc.perform(post("/api/ponto/saida")
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horaSaida").exists());
    }

    @Test
    @Order(22)
    void baterPonto_saidaDuplicada_deveRetornar409() throws Exception {
        mockMvc.perform(post("/api/ponto/saida")
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(23)
    void listarMeusRegistrosDePonto_deveRetornarUmRegistroCompleto() throws Exception {
        mockMvc.perform(get("/api/ponto/meus")
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].horaEntrada").exists())
                .andExpect(jsonPath("$[0].horaSaida").exists());
    }

    // ---------------------------------------------------------------
    // Folha de hora (PDF)
    // ---------------------------------------------------------------

    @Test
    @Order(24)
    void folhaHoraPropria_deveGerarPdfValido() throws Exception {
        var hoje = java.time.LocalDate.now().toString();

        MvcResult resultado = mockMvc.perform(get("/api/ponto/folha-hora")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .param("inicio", hoje)
                        .param("fim", hoje))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();

        byte[] pdf = resultado.getResponse().getContentAsByteArray();
        // valida que e um PDF de verdade (assinatura magica %PDF) e
        // que nao esta vazio/truncado
        assertThat(pdf.length).isGreaterThan(500);
        assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    @Order(25)
    void folhaHoraDeOutroUsuario_comoFuncionario_deveRetornar403() throws Exception {
        var hoje = java.time.LocalDate.now().toString();

        mockMvc.perform(get("/api/ponto/folha-hora")
                        .header("Authorization", "Bearer " + tokenFuncionario)
                        .param("usuarioId", "1") // id do admin
                        .param("inicio", hoje)
                        .param("fim", hoje))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(26)
    void folhaHoraDeOutroUsuario_comoAdmin_deveFuncionar() throws Exception {
        var hoje = java.time.LocalDate.now().toString();

        mockMvc.perform(get("/api/ponto/folha-hora")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .param("usuarioId", funcionarioId.toString())
                        .param("inicio", hoje)
                        .param("fim", hoje))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    // ---------------------------------------------------------------
    // Listagem de usuarios (com paginacao)
    // ---------------------------------------------------------------

    @Test
    @Order(27)
    void listarUsuarios_comoAdmin_deveRetornarListaComAdminEFuncionario() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(2))
                // nenhum campo de senha deve vazar na resposta
                .andExpect(jsonPath("$.content[0].senhaHash").doesNotExist())
                .andExpect(jsonPath("$.content[0].senha").doesNotExist());
    }

    @Test
    @Order(28)
    void listarUsuarios_comoFuncionario_deveRetornar403() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + tokenFuncionario))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------
    // Health check (publico)
    // ---------------------------------------------------------------

    @Test
    @Order(29)
    void healthCheck_deveRetornar200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
