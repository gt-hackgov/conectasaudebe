package br.com.conectasaude;

import br.com.conectasaude.dto.auth.LoginRequest;
import br.com.conectasaude.dto.chatbot.ChatbotRequest;
import br.com.conectasaude.model.FaqEntry;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.UnansweredQuestion;
import br.com.conectasaude.repository.FaqEntryRepository;
import br.com.conectasaude.repository.UnansweredQuestionRepository;
import br.com.conectasaude.repository.UsuarioRepository;
import br.com.conectasaude.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.jwt.expiration-minutes=60"
})
@AutoConfigureMockMvc
@DisplayName("Chatbot Integration Tests")
class ChatbotIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FaqEntryRepository faqRepository;

    @Autowired
    private UnansweredQuestionRepository unansweredRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String authToken;
    private String testCpf;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up
        faqRepository.deleteAll();
        unansweredRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Create unique CPF for each test
        testCpf = String.format("%0" + 11 + "d", new Random().nextInt(99999999));

        // Create and login user
        usuarioService.cadastrarUsuario(
                testCpf,
                "Test User",
                "password123",
                Role.PACIENTE
        );

        // Get token
        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest(testCpf, "password123")
                        ))
        ).andReturn();

        JsonNode jsonResponse = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        authToken = jsonResponse.get("token").asText();

        // Seed FAQ data
        FaqEntry faq1 = new FaqEntry(
                "Como faço para agendar um clínico geral?",
                "Você pode agendar pelo aplicativo na aba Agendar Consulta",
                "AGENDAMENTO",
                Arrays.asList("agendar", "clinico", "consulta")
        );

        FaqEntry faq2 = new FaqEntry(
                "Preciso fazer jejum para exame de sangue?",
                "O jejum recomendado é de 8 a 12 horas",
                "EXAMES",
                Arrays.asList("jejum", "exame", "sangue")
        );

        faqRepository.save(faq1);
        faqRepository.save(faq2);
    }

    @Test
    @DisplayName("Deve retornar resposta FAQ quando encontrada por keyword")
    void testChatbotWithValidKeyword() throws Exception {
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage("Como agendar uma consulta?");

        MvcResult result = mockMvc.perform(
                post("/api/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matched").value(true))
                .andExpect(jsonPath("$.category").value("AGENDAMENTO"))
                .andExpect(jsonPath("$.answer").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);

        assertThat(jsonResponse.get("matched").asBoolean()).isTrue();
        assertThat(jsonResponse.get("category").asText()).isEqualTo("AGENDAMENTO");
        assertThat(jsonResponse.get("answer").asText()).contains("aplicativo");
    }

    @Test
    @DisplayName("Deve retornar resposta padrão quando FAQ não é encontrada")
    void testChatbotWithUnknownQuestion() throws Exception {
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage("xyz aleatório pqr não existe");

        MvcResult result = mockMvc.perform(
                post("/api/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matched").value(false))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonResponse = objectMapper.readTree(responseBody);

        assertThat(jsonResponse.get("matched").asBoolean()).isFalse();
        assertThat(jsonResponse.get("answer").asText()).contains("atendente");
    }

    @Test
    @DisplayName("Deve registrar pergunta não respondida")
    void testUnansweredQuestionTracking() throws Exception {
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage("xyz totalmente aleatório pqr");

        mockMvc.perform(
                post("/api/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk());

        long count = unansweredRepository.count();
        assertThat(count).isGreaterThan(0);

        UnansweredQuestion question = unansweredRepository.findAll().get(0);
        assertThat(question.getUserMessage()).isEqualTo("xyz totalmente aleatório pqr");
    }

    @Test
    @DisplayName("Deve ser case-insensitive na busca")
    void testCaseInsensitiveSearch() throws Exception {
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage("COMO AGENDAR??");

        mockMvc.perform(
                post("/api/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + authToken)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matched").value(true));
    }

    @Test
    @DisplayName("Deve retornar 401 sem autenticação")
    void testUnauthorizedAccess() throws Exception {
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage("Teste sem autenticação");

        mockMvc.perform(
                post("/api/chatbot/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isUnauthorized());
    }
}
