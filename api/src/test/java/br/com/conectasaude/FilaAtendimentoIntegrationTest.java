package br.com.conectasaude;

import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.Usuario;
import br.com.conectasaude.repository.AgendamentoRepository;
import br.com.conectasaude.repository.AuditLogRepository;
import br.com.conectasaude.repository.UsuarioRepository;
import br.com.conectasaude.service.FilaAtendimentoService;
import br.com.conectasaude.service.JwtService;
import br.com.conectasaude.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=60",
        "app.indicadores.dias-historico=0"
})
@AutoConfigureMockMvc
class FilaAtendimentoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private JwtService jwtService;
    @Autowired private FilaAtendimentoService filaService;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private String tokenMedico;

    @BeforeEach
    void preparar() {
        filaService.limpar();
        agendamentoRepository.deleteAll();
        auditLogRepository.deleteAll();
        usuarioRepository.deleteAll();
        Usuario medico = usuarioService.cadastrarUsuario("44444444444", "Medico Fila", "SenhaTeste1!", Role.MEDICO);
        tokenMedico = jwtService.gerarToken(medico);
    }

    private void entrar(String iniciais, String risco) throws Exception {
        mockMvc.perform(post("/api/medico/fila")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"iniciais\":\"" + iniciais + "\",\"classificacao\":\"" + risco + "\",\"queixa\":\"teste\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void filaOrdenaPorRiscoEDepoisPorChegada() throws Exception {
        entrar("A.A.", "VERDE");
        entrar("B.B.", "AMARELO");
        entrar("C.C.", "VERDE");
        entrar("D.D.", "VERMELHO");

        mockMvc.perform(get("/api/medico/fila").header("Authorization", "Bearer " + tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fila", hasSize(4)))
                .andExpect(jsonPath("$.fila[0].iniciais").value("D.D."))
                .andExpect(jsonPath("$.fila[1].iniciais").value("B.B."))
                .andExpect(jsonPath("$.fila[2].iniciais").value("A.A."))
                .andExpect(jsonPath("$.fila[3].iniciais").value("C.C."));
    }

    @Test
    void chamarProximoRemoveOMaisPrioritario() throws Exception {
        entrar("A.A.", "AZUL");
        entrar("B.B.", "LARANJA");

        mockMvc.perform(post("/api/medico/fila/chamar").header("Authorization", "Bearer " + tokenMedico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chamado.iniciais").value("B.B."));

        mockMvc.perform(get("/api/medico/fila").header("Authorization", "Bearer " + tokenMedico))
                .andExpect(jsonPath("$.fila", hasSize(1)));
    }

    @Test
    void chamarComFilaVaziaRetorna404() throws Exception {
        mockMvc.perform(post("/api/medico/fila/chamar").header("Authorization", "Bearer " + tokenMedico))
                .andExpect(status().isNotFound());
    }

    @Test
    void entradaInvalidaRetorna400() throws Exception {
        mockMvc.perform(post("/api/medico/fila")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"iniciais\":\"\",\"classificacao\":\"VERDE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pacienteNaoAcessaAFila() throws Exception {
        Usuario paciente = usuarioService.cadastrarUsuario("55555555555", "Paciente", "SenhaTeste1!", Role.PACIENTE);
        mockMvc.perform(get("/api/medico/fila").header("Authorization", "Bearer " + jwtService.gerarToken(paciente)))
                .andExpect(status().isForbidden());
    }
}
