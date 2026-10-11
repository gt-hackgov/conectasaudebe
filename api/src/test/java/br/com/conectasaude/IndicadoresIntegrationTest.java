package br.com.conectasaude;

import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.Usuario;
import br.com.conectasaude.repository.AgendamentoRepository;
import br.com.conectasaude.repository.AuditLogRepository;
import br.com.conectasaude.repository.UsuarioRepository;
import br.com.conectasaude.service.JwtService;
import br.com.conectasaude.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=60",
        "app.indicadores.dias-historico=30"
})
@AutoConfigureMockMvc
class IndicadoresIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioService usuarioService;
    @Autowired private JwtService jwtService;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    @BeforeEach
    void limpar() {
        agendamentoRepository.deleteAll();
        auditLogRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void adminRecebeAsSeisConsultasComResultados() throws Exception {
        Usuario admin = usuarioService.cadastrarUsuario("66666666666", "Admin", "SenhaTeste1!", Role.ADMIN);

        mockMvc.perform(get("/api/admin/indicadores").header("Authorization", "Bearer " + jwtService.gerarToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.totalAtendimentos", greaterThan(0)))
                .andExpect(jsonPath("$.consultas", hasSize(6)))
                .andExpect(jsonPath("$.consultas[0].linhas", hasSize(6)))
                .andExpect(jsonPath("$.consultas[2].linhas[0].unidade").value("UBS Parque das Flores"))
                .andExpect(jsonPath("$.consultas[3].linhas[0].posicao").value(1));
    }

    @Test
    void medicoNaoAcessaIndicadores() throws Exception {
        Usuario medico = usuarioService.cadastrarUsuario("77777777777", "Medico", "SenhaTeste1!", Role.MEDICO);

        mockMvc.perform(get("/api/admin/indicadores").header("Authorization", "Bearer " + jwtService.gerarToken(medico)))
                .andExpect(status().isForbidden());
    }
}
