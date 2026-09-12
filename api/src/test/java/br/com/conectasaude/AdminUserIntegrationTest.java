package br.com.conectasaude;

import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.Usuario;
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
class AdminUserIntegrationTest {

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

    @Autowired
    private JwtService jwtService;

    private String tokenAdmin;

    @BeforeEach
    void limparBase() {
        agendamentoRepository.deleteAll();
        notificacaoRepository.deleteAll();
        auditLogRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario admin = usuarioService.cadastrarUsuario(
                "11111111111",
                "Admin Teste",
                "SenhaTeste1!",
                Role.ADMIN
        );
        tokenAdmin = jwtService.gerarToken(admin);
    }

    @Test
    void admin_consegueCriarMedico() throws Exception {
        Map<String, String> request = Map.of(
                "cpf", "22222222222",
                "nome", "Medico Novo",
                "senha", "SenhaTeste1!",
                "role", "MEDICO"
        );

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("Medico Novo"))
                .andExpect(jsonPath("$.cpf").value("22222222222"))
                .andExpect(jsonPath("$.role").value("MEDICO"));

        Optional<Usuario> usuario =
                usuarioRepository.findByCpf("22222222222");

        assertThat(usuario).isPresent();
        assertThat(usuario.get().getRole()).isEqualTo(Role.MEDICO);
    }

    @Test
    void admin_consegueCriarAdmin() throws Exception {
        Map<String, String> request = Map.of(
                "cpf", "33333333333",
                "nome", "Admin Novo",
                "senha", "SenhaTeste1!",
                "role", "ADMIN"
        );

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        Optional<Usuario> usuario =
                usuarioRepository.findByCpf("33333333333");

        assertThat(usuario).isPresent();
        assertThat(usuario.get().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void admin_consegueCriarAuditor() throws Exception {
        Map<String, String> request = Map.of(
                "cpf", "44444444444",
                "nome", "Auditor Novo",
                "senha", "SenhaTeste1!",
                "role", "AUDITOR"
        );

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("AUDITOR"));

        Optional<Usuario> usuario =
                usuarioRepository.findByCpf("44444444444");

        assertThat(usuario).isPresent();
        assertThat(usuario.get().getRole()).isEqualTo(Role.AUDITOR);
    }

    @Test
    void paciente_naoConsegueUsarEndpoint() throws Exception {
        Usuario paciente = usuarioService.cadastrarUsuario(
                "55555555555",
                "Paciente Teste",
                "SenhaTeste1!",
                Role.PACIENTE
        );
        String tokenPaciente = jwtService.gerarToken(paciente);

        Map<String, String> request = Map.of(
                "cpf", "66666666666",
                "nome", "Medico Bloqueado",
                "senha", "SenhaTeste1!",
                "role", "MEDICO"
        );

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenPaciente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void medico_naoConsegueUsarEndpoint() throws Exception {
        Usuario medico = usuarioService.cadastrarUsuario(
                "77777777777",
                "Medico Teste",
                "SenhaTeste1!",
                Role.MEDICO
        );
        String tokenMedico = jwtService.gerarToken(medico);

        Map<String, String> request = Map.of(
                "cpf", "88888888888",
                "nome", "Medico Bloqueado",
                "senha", "SenhaTeste1!",
                "role", "MEDICO"
        );

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void semToken_retorna401() throws Exception {
        Map<String, String> request = Map.of(
                "cpf", "99999999999",
                "nome", "Sem Token",
                "senha", "SenhaTeste1!",
                "role", "MEDICO"
        );

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void criarPaciente_retorna400() throws Exception {
        Map<String, String> request = Map.of(
                "cpf", "10101010101",
                "nome", "Paciente Negado",
                "senha", "SenhaTeste1!",
                "role", "PACIENTE"
        );

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Dados inválidos"));

        assertThat(usuarioRepository.findByCpf("10101010101")).isEmpty();
    }

    @Test
    void cpfDuplicado_retorna409() throws Exception {
        usuarioService.cadastrarUsuario(
                "12121212121",
                "Usuario Existente",
                "SenhaTeste1!",
                Role.MEDICO
        );

        Map<String, String> request = Map.of(
                "cpf", "12121212121",
                "nome", "Outro Usuario",
                "senha", "SenhaTeste2!",
                "role", "ADMIN"
        );

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("CPF já cadastrado"));
    }

    @Test
    void resposta_nuncaContemSenha() throws Exception {
        Map<String, String> request = Map.of(
                "cpf", "13131313131",
                "nome", "Medico Seguro",
                "senha", "SenhaTeste1!",
                "role", "MEDICO"
        );

        MvcResult result = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenAdmin)
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
}
