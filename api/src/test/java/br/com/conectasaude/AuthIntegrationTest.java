package br.com.conectasaude;

import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.Usuario;
import br.com.conectasaude.repository.AgendamentoRepository;
import br.com.conectasaude.repository.AuditLogRepository;
import br.com.conectasaude.repository.NotificacaoRepository;
import br.com.conectasaude.repository.UsuarioRepository;
import br.com.conectasaude.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=60"
})
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UsuarioService usuarioService;

    @BeforeEach
    void limparBase() {
        agendamentoRepository.deleteAll();
        notificacaoRepository.deleteAll();
        auditLogRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void register_valido_criaPaciente() throws Exception {
        Map<String, String> request = Map.of(
                "cpf", "12345678901",
                "nome", "Paciente Novo",
                "senha", "SenhaTeste1!"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Paciente Novo"))
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.role").value("PACIENTE"));

        Optional<Usuario> usuario =
                usuarioRepository.findByCpf("12345678901");

        assertThat(usuario).isPresent();
        assertThat(usuario.get().getRole()).isEqualTo(Role.PACIENTE);
    }

    @Test
    void register_valido_naoRetornaSenhaNemHash() throws Exception {
        Map<String, String> request = Map.of(
                "cpf", "12345678901",
                "nome", "Paciente Novo",
                "senha", "SenhaTeste1!"
        );

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        JsonNode body = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        assertThat(body.has("senha")).isFalse();
        assertThat(body.has("passwordHash")).isFalse();
        assertThat(body.has("password")).isFalse();
        assertThat(body.toString()).doesNotContain("SenhaTeste1!");
        assertThat(body.toString()).doesNotContain("passwordHash");
    }

    @Test
    void register_cpfDuplicado_retorna409() throws Exception {
        usuarioService.cadastrarUsuario(
                "12345678901",
                "Paciente Existente",
                "SenhaTeste1!",
                Role.PACIENTE
        );

        Map<String, String> request = Map.of(
                "cpf", "12345678901",
                "nome", "Outro Paciente",
                "senha", "SenhaTeste2!"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("CPF já cadastrado"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void register_requestInvalido_retorna400() throws Exception {
        Map<String, String> request = Map.of(
                "cpf", "",
                "nome", "",
                "senha", ""
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Dados inválidos"));
    }

    @Test
    void register_comRoleNoRequest_ignoraECriaPaciente() throws Exception {
        String payload = """
                {
                  "cpf": "12345678901",
                  "nome": "Tentativa Medico",
                  "senha": "SenhaTeste1!",
                  "role": "MEDICO"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("PACIENTE"));

        Optional<Usuario> usuario =
                usuarioRepository.findByCpf("12345678901");

        assertThat(usuario).isPresent();
        assertThat(usuario.get().getRole()).isEqualTo(Role.PACIENTE);
    }

    @Test
    void register_comRoleAdminNoRequest_ignoraECriaPaciente() throws Exception {
        String payload = """
                {
                  "cpf": "98765432100",
                  "nome": "Tentativa Admin",
                  "senha": "SenhaTeste1!",
                  "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("PACIENTE"));

        Optional<Usuario> usuario =
                usuarioRepository.findByCpf("98765432100");

        assertThat(usuario).isPresent();
        assertThat(usuario.get().getRole()).isEqualTo(Role.PACIENTE);
    }
}
