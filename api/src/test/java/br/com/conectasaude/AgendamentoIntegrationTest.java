package br.com.conectasaude;

import br.com.conectasaude.dto.agendamento.AgendamentoRequest;
import br.com.conectasaude.dto.agendamento.AgendamentoResponse;
import br.com.conectasaude.model.Agendamento;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.Usuario;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditLog;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.repository.AgendamentoRepository;
import br.com.conectasaude.repository.AuditLogRepository;
import br.com.conectasaude.repository.UsuarioRepository;
import br.com.conectasaude.service.AgendamentoService;
import br.com.conectasaude.service.JwtService;
import br.com.conectasaude.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=60"
})
@AutoConfigureMockMvc
class AgendamentoIntegrationTest {

    private static final DateTimeFormatter FORMATO_HORARIO =
            DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void limparBase() {
        agendamentoRepository.deleteAll();
        auditLogRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void getAppointments_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAppointments_comPerfilMedico_retorna403() throws Exception {
        Usuario medico = usuarioService.cadastrarUsuario(
                "11111111111",
                "Medico Teste",
                "SenhaTeste1!",
                Role.MEDICO
        );
        String token = jwtService.gerarToken(medico);

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAppointments_comPerfilPaciente_retorna200() throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        String token = jwtService.gerarToken(paciente);

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments").isArray());
    }

    @Test
    void getAppointments_isolaAgendamentosEntrePacientes() throws Exception {
        Usuario pacienteA = criarPaciente("22222222222", "Paciente A");
        Usuario pacienteB = criarPaciente("33333333333", "Paciente B");

        AgendamentoResponse agendamentoA = agendamentoService.criar(
                criarRequestFuturo("UBS Centro", "Clínica Geral"),
                pacienteA.getId()
        );
        agendamentoService.criar(
                criarRequestFuturo("UBS Norte", "Pediatria"),
                pacienteB.getId()
        );

        String tokenA = jwtService.gerarToken(pacienteA);

        mockMvc.perform(get("/api/appointments")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointments", hasSize(1)))
                .andExpect(jsonPath("$.appointments[0].id")
                        .value(agendamentoA.id()))
                .andExpect(jsonPath("$.appointments[0].location")
                        .value("UBS Centro"));
    }

    @Test
    void postAppointments_pacienteCriaAgendamentoAssociadoAoUsuario()
            throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        String token = jwtService.gerarToken(paciente);

        AgendamentoRequest request = criarRequestFuturo(
                "UBS Centro",
                "Clínica Geral"
        );

        String body = objectMapper.writeValueAsString(Map.of(
                "date", request.date(),
                "time", request.time(),
                "location", request.location(),
                "specialty", request.specialty(),
                "notes", "Observação de teste",
                "usuarioId", "00000000-0000-0000-0000-000000000099"
        ));

        String response = mockMvc.perform(post("/api/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Agendamento criado com sucesso"))
                .andExpect(jsonPath("$.appointment.id").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(response)
                .get("appointment")
                .get("id")
                .asString();

        Agendamento salvo = agendamentoRepository
                .findByIdAndUsuarioId(id, paciente.getId())
                .orElseThrow();

        assertThat(salvo.getId()).isEqualTo(id);
        assertThat(agendamentoRepository.count()).isEqualTo(1);
    }

    @Test
    void deleteAppointments_excluiProprioAgendamento() throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        AgendamentoResponse criado = agendamentoService.criar(
                criarRequestFuturo("UBS Centro", "Clínica Geral"),
                paciente.getId()
        );
        String token = jwtService.gerarToken(paciente);

        mockMvc.perform(delete("/api/appointments/" + criado.id())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Consulta cancelada com sucesso"));

        assertThat(agendamentoRepository.findById(criado.id())).isEmpty();
    }

    @Test
    void deleteAppointments_agendamentoDeOutroPaciente_retornaNaoEncontrado()
            throws Exception {
        Usuario pacienteA = criarPaciente("22222222222", "Paciente A");
        Usuario pacienteB = criarPaciente("33333333333", "Paciente B");

        AgendamentoResponse agendamentoB = agendamentoService.criar(
                criarRequestFuturo("UBS Norte", "Pediatria"),
                pacienteB.getId()
        );

        String tokenA = jwtService.gerarToken(pacienteA);

        mockMvc.perform(delete("/api/appointments/" + agendamentoB.id())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Consulta não encontrada."));

        assertThat(agendamentoRepository.findById(agendamentoB.id()))
                .isPresent();
    }

    @Test
    void deleteAppointments_idInexistente_retornaNaoEncontrado()
            throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        String token = jwtService.gerarToken(paciente);

        mockMvc.perform(delete("/api/appointments/apt-inexistente")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Consulta não encontrada."));
    }

    @Test
    void putAppointments_pacienteAtualizaProprioAgendamento() throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        AgendamentoResponse criado = agendamentoService.criar(
                criarRequestFuturo("UBS Centro", "Clínica Geral"),
                paciente.getId()
        );
        String createdAtOriginal = criado.createdAt();
        String token = jwtService.gerarToken(paciente);

        AgendamentoRequest atualizacao = criarRequestFuturo(
                "Hospital Municipal",
                "Cardiologia",
                3,
                14,
                "Nova observação"
        );

        mockMvc.perform(put("/api/appointments/" + criado.id())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Agendamento atualizado com sucesso"))
                .andExpect(jsonPath("$.appointment.id").value(criado.id()))
                .andExpect(jsonPath("$.appointment.date")
                        .value(atualizacao.date()))
                .andExpect(jsonPath("$.appointment.time")
                        .value(atualizacao.time()))
                .andExpect(jsonPath("$.appointment.location")
                        .value("Hospital Municipal"))
                .andExpect(jsonPath("$.appointment.specialty")
                        .value("Cardiologia"))
                .andExpect(jsonPath("$.appointment.notes")
                        .value("Nova observação"))
                .andExpect(jsonPath("$.appointment.createdAt")
                        .value(createdAtOriginal));

        Agendamento persistido = agendamentoRepository
                .findByIdAndUsuarioId(criado.id(), paciente.getId())
                .orElseThrow();

        assertThat(persistido.getId()).isEqualTo(criado.id());
        assertThat(persistido.getDate()).isEqualTo(atualizacao.date());
        assertThat(persistido.getTime()).isEqualTo(atualizacao.time());
        assertThat(persistido.getLocation()).isEqualTo("Hospital Municipal");
        assertThat(persistido.getSpecialty()).isEqualTo("Cardiologia");
        assertThat(persistido.getNotes()).isEqualTo("Nova observação");
        assertThat(persistido.getCreatedAt()).isEqualTo(createdAtOriginal);
        assertThat(agendamentoRepository.count()).isEqualTo(1);
    }

    @Test
    void putAppointments_agendamentoDeOutroPaciente_retornaNaoEncontrado()
            throws Exception {
        Usuario pacienteA = criarPaciente("22222222222", "Paciente A");
        Usuario pacienteB = criarPaciente("33333333333", "Paciente B");

        AgendamentoResponse agendamentoB = agendamentoService.criar(
                criarRequestFuturo("UBS Norte", "Pediatria"),
                pacienteB.getId()
        );

        String tokenA = jwtService.gerarToken(pacienteA);
        AgendamentoRequest atualizacao = criarRequestFuturo(
                "Hospital Municipal",
                "Cardiologia",
                3,
                14,
                "Tentativa indevida"
        );

        mockMvc.perform(put("/api/appointments/" + agendamentoB.id())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(atualizacao)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Consulta não encontrada."));

        Agendamento persistido = agendamentoRepository
                .findByIdAndUsuarioId(agendamentoB.id(), pacienteB.getId())
                .orElseThrow();

        assertThat(persistido.getLocation()).isEqualTo("UBS Norte");
        assertThat(persistido.getSpecialty()).isEqualTo("Pediatria");
        assertThat(persistido.getNotes()).isEqualTo("Observação de teste");
        assertThat(agendamentoRepository
                .findByIdAndUsuarioId(agendamentoB.id(), pacienteA.getId()))
                .isEmpty();
    }

    @Test
    void putAppointments_idInexistente_retornaNaoEncontrado() throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        String token = jwtService.gerarToken(paciente);

        mockMvc.perform(put("/api/appointments/apt-inexistente")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(
                                criarRequestFuturo(
                                        "UBS Centro",
                                        "Clínica Geral"
                                )
                        )))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error")
                        .value("Consulta não encontrada."));
    }

    @Test
    void putAppointments_dadosObrigatoriosInvalidos_retorna400()
            throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        AgendamentoResponse criado = agendamentoService.criar(
                criarRequestFuturo("UBS Centro", "Clínica Geral"),
                paciente.getId()
        );
        String token = jwtService.gerarToken(paciente);

        mockMvc.perform(put("/api/appointments/" + criado.id())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "date", "",
                                "time", "",
                                "location", "",
                                "specialty", ""
                        ))))
                .andExpect(status().isBadRequest());

        Agendamento persistido = agendamentoRepository
                .findById(criado.id())
                .orElseThrow();
        assertThat(persistido.getLocation()).isEqualTo("UBS Centro");
    }

    @Test
    void putAppointments_dataHoraNoPassado_retorna400() throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        AgendamentoResponse criado = agendamentoService.criar(
                criarRequestFuturo("UBS Centro", "Clínica Geral"),
                paciente.getId()
        );
        String token = jwtService.gerarToken(paciente);

        LocalDateTime passado = LocalDateTime.now()
                .minusDays(1)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        mockMvc.perform(put("/api/appointments/" + criado.id())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "date", passado.toLocalDate().toString(),
                                "time", passado.toLocalTime()
                                        .format(FORMATO_HORARIO),
                                "location", "UBS Centro",
                                "specialty", "Clínica Geral",
                                "notes", "Observação de teste"
                        ))))
                .andExpect(status().isBadRequest());

        Agendamento persistido = agendamentoRepository
                .findById(criado.id())
                .orElseThrow();
        assertThat(persistido.getDate()).isEqualTo(criado.date());
        assertThat(persistido.getTime()).isEqualTo(criado.time());
    }

    @Test
    void putAppointments_comPerfilMedico_retorna403() throws Exception {
        Usuario medico = usuarioService.cadastrarUsuario(
                "11111111111",
                "Medico Teste",
                "SenhaTeste1!",
                Role.MEDICO
        );
        String token = jwtService.gerarToken(medico);

        mockMvc.perform(put("/api/appointments/apt-qualquer")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(
                                criarRequestFuturo(
                                        "UBS Centro",
                                        "Clínica Geral"
                                )
                        )))
                .andExpect(status().isForbidden());
    }

    @Test
    void putAppointments_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(put("/api/appointments/apt-qualquer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(
                                criarRequestFuturo(
                                        "UBS Centro",
                                        "Clínica Geral"
                                )
                        )))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void putAppointments_atualizacaoBemSucedida_registraAuditoriaSucesso()
            throws Exception {
        Usuario paciente = criarPaciente("22222222222", "Paciente Teste");
        AgendamentoResponse criado = agendamentoService.criar(
                criarRequestFuturo("UBS Centro", "Clínica Geral"),
                paciente.getId()
        );
        String token = jwtService.gerarToken(paciente);

        mockMvc.perform(put("/api/appointments/" + criado.id())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(
                                criarRequestFuturo(
                                        "Hospital Municipal",
                                        "Cardiologia"
                                )
                        )))
                .andExpect(status().isOk());

        List<AuditLog> logs = auditLogRepository.findAll().stream()
                .filter(log ->
                        log.getAcao() == AuditAction.ATUALIZAR_REGISTRO
                )
                .toList();

        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getResultado())
                .isEqualTo(AuditResult.SUCESSO);
        assertThat(logs.getFirst().getRecurso())
                .isEqualTo("AGENDAMENTOS");
        assertThat(logs.getFirst().getUsuarioId())
                .isEqualTo(paciente.getId());
        assertThat(logs.getFirst().getPerfil())
                .isEqualTo(Role.PACIENTE);
        assertThat(logs.getFirst().getMetodoHttp())
                .isEqualTo("PUT");
    }

    @Test
    void putAppointments_semPropriedade_registraAuditoriaNegado()
            throws Exception {
        Usuario pacienteA = criarPaciente("22222222222", "Paciente A");
        Usuario pacienteB = criarPaciente("33333333333", "Paciente B");

        AgendamentoResponse agendamentoB = agendamentoService.criar(
                criarRequestFuturo("UBS Norte", "Pediatria"),
                pacienteB.getId()
        );

        String tokenA = jwtService.gerarToken(pacienteA);

        mockMvc.perform(put("/api/appointments/" + agendamentoB.id())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAgendamento(
                                criarRequestFuturo(
                                        "Hospital Municipal",
                                        "Cardiologia"
                                )
                        )))
                .andExpect(status().isNotFound());

        List<AuditLog> logs = auditLogRepository.findAll().stream()
                .filter(log ->
                        log.getAcao() == AuditAction.ATUALIZAR_REGISTRO
                )
                .toList();

        assertThat(logs).hasSize(1);
        assertThat(logs.getFirst().getResultado())
                .isEqualTo(AuditResult.NEGADO);
        assertThat(logs.getFirst().getRecurso())
                .isEqualTo("AGENDAMENTOS");
        assertThat(logs.getFirst().getUsuarioId())
                .isEqualTo(pacienteA.getId());
        assertThat(logs.getFirst().getPerfil())
                .isEqualTo(Role.PACIENTE);
        assertThat(logs.getFirst().getMetodoHttp())
                .isEqualTo("PUT");
    }

    private Usuario criarPaciente(String cpf, String nome) {
        return usuarioService.cadastrarUsuario(
                cpf,
                nome,
                "SenhaTeste1!",
                Role.PACIENTE
        );
    }

    private AgendamentoRequest criarRequestFuturo(
            String location,
            String specialty
    ) {
        return criarRequestFuturo(
                location,
                specialty,
                1,
                10,
                "Observação de teste"
        );
    }

    private AgendamentoRequest criarRequestFuturo(
            String location,
            String specialty,
            int dias,
            int hora,
            String notes
    ) {
        LocalDateTime futuro = LocalDateTime.now()
                .plusDays(dias)
                .withHour(hora)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        return new AgendamentoRequest(
                futuro.toLocalDate().toString(),
                futuro.toLocalTime().format(FORMATO_HORARIO),
                location,
                specialty,
                notes
        );
    }

    private String corpoAgendamento(AgendamentoRequest request) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "date", request.date(),
                "time", request.time(),
                "location", request.location(),
                "specialty", request.specialty(),
                "notes", request.notes()
        ));
    }
}
