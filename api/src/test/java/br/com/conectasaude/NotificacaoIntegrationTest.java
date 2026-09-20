package br.com.conectasaude;

import br.com.conectasaude.model.Notificacao;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.TipoNotificacao;
import br.com.conectasaude.model.Usuario;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditLog;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.repository.AgendamentoRepository;
import br.com.conectasaude.repository.AuditLogRepository;
import br.com.conectasaude.repository.NotificacaoRepository;
import br.com.conectasaude.repository.UsuarioRepository;
import br.com.conectasaude.service.JwtService;
import br.com.conectasaude.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=60"
})
@AutoConfigureMockMvc
class NotificacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void limparBase() {
        agendamentoRepository.deleteAll();
        notificacaoRepository.deleteAll();
        auditLogRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void getNotifications_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getNotifications_comPerfilMedico_retorna403() throws Exception {
        Usuario medico = usuarioService.cadastrarUsuario(
                "11111111111",
                "Medico Teste",
                "SenhaTeste1!",
                Role.MEDICO
        );
        String token = jwtService.gerarToken(medico);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getNotifications_comPerfilPaciente_retorna200() throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        String token = jwtService.gerarToken(paciente);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications").isArray());
    }

    @Test
    void getNotifications_retornaNotificacaoGlobal() throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        salvarGlobal("notif-global-1", "Campanha Global");

        String token = jwtService.gerarToken(paciente);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(1)))
                .andExpect(jsonPath("$.notifications[0].id")
                        .value("notif-global-1"))
                .andExpect(jsonPath("$.notifications[0].title")
                        .value("Campanha Global"));
    }

    @Test
    void getNotifications_retornaNotificacaoPessoalDoPaciente() throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente A");
        salvarPessoal(
                "notif-pessoal-a",
                "Lembrete Pessoal",
                paciente
        );

        String token = jwtService.gerarToken(paciente);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(1)))
                .andExpect(jsonPath("$.notifications[0].id")
                        .value("notif-pessoal-a"))
                .andExpect(jsonPath("$.notifications[0].title")
                        .value("Lembrete Pessoal"));
    }

    @Test
    void getNotifications_isolaNotificacoesEntrePacientes() throws Exception {
        Usuario pacienteA = criarPaciente("22222222222", "Paciente A");
        Usuario pacienteB = criarPaciente("33333333333", "Paciente B");

        salvarGlobal("notif-global-1", "Campanha Global");
        salvarPessoal("notif-pessoal-a", "Pessoal A", pacienteA);
        salvarPessoal("notif-pessoal-b", "Pessoal B", pacienteB);

        String tokenA = jwtService.gerarToken(pacienteA);
        String tokenB = jwtService.gerarToken(pacienteB);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(2)))
                .andExpect(jsonPath("$.notifications[*].id", containsInAnyOrder(
                        "notif-global-1",
                        "notif-pessoal-a"
                )));

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(2)))
                .andExpect(jsonPath("$.notifications[*].id", containsInAnyOrder(
                        "notif-global-1",
                        "notif-pessoal-b"
                )));
    }

    @Test
    void getNotifications_naoExpoeCamposSensiveisNaResposta() throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        salvarGlobal("notif-global-1", "Campanha Global");
        String token = jwtService.gerarToken(paciente);

        MvcResult result = mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(1)))
                .andExpect(jsonPath("$.notifications[0].usuario").doesNotExist())
                .andExpect(jsonPath("$.notifications[0].usuarioId").doesNotExist())
                .andExpect(jsonPath("$.notifications[0].cpf").doesNotExist())
                .andExpect(jsonPath("$.notifications[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.notifications[0].tipo").doesNotExist())
                .andReturn();

        JsonNode notificacao = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("notifications")
                .get(0);

        assertThat(notificacao.propertyNames()).containsExactlyInAnyOrder(
                "id",
                "title",
                "message",
                "time"
        );
    }

    @Test
    void getNotifications_registraAuditoriaDeConsulta() throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        String token = jwtService.gerarToken(paciente);

        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        List<AuditLog> logs = auditLogRepository.findAll().stream()
                .filter(log ->
                        log.getAcao() == AuditAction.VISUALIZAR_DADO_SENSIVEL
                )
                .toList();

        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getResultado())
                .isEqualTo(AuditResult.SUCESSO);
        assertThat(logs.getFirst().getRecurso())
                .isEqualTo("NOTIFICACOES");
        assertThat(logs.getFirst().getUsuarioId())
                .isEqualTo(paciente.getId());
        assertThat(logs.getFirst().getPerfil())
                .isEqualTo(Role.PACIENTE);
    }

    private Usuario criarPaciente(String cpf, String nome) {
        return usuarioService.cadastrarUsuario(
                cpf,
                nome,
                "SenhaTeste1!",
                Role.PACIENTE
        );
    }

    private void salvarGlobal(String id, String title) {
        notificacaoRepository.save(new Notificacao(
                id,
                title,
                "Mensagem de teste global",
                "Agora",
                TipoNotificacao.GLOBAL
        ));
    }

    private void salvarPessoal(String id, String title, Usuario usuario) {
        Notificacao notificacao = new Notificacao(
                id,
                title,
                "Mensagem de teste pessoal",
                "Há 1 hora",
                TipoNotificacao.PESSOAL
        );
        notificacao.setUsuario(usuario);
        notificacaoRepository.save(notificacao);
    }
}
