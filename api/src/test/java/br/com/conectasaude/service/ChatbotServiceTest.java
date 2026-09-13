package br.com.conectasaude.service;

import br.com.conectasaude.dto.chatbot.ChatbotResponse;
import br.com.conectasaude.model.FaqEntry;
import br.com.conectasaude.model.UnansweredQuestion;
import br.com.conectasaude.repository.FaqEntryRepository;
import br.com.conectasaude.repository.UnansweredQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Chatbot Service Tests")
class ChatbotServiceTest {

    @Mock
    private FaqEntryRepository faqRepository;

    @Mock
    private UnansweredQuestionRepository unansweredRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    private FaqEntry faqAgendamento;
    private FaqEntry faqExame;

    @BeforeEach
    void setUp() {
        faqAgendamento = new FaqEntry(
                "Como faço para agendar um clínico geral na UBS do meu bairro?",
                "Você pode agendar pelo aplicativo, na aba \"Agendar Consulta\"...",
                "AGENDAMENTO",
                Arrays.asList("agendar", "clinico geral", "ubs", "marcar consulta", "consulta")
        );
        faqAgendamento.setId(1L);

        faqExame = new FaqEntry(
                "Preciso fazer jejum de quantas horas para o exame de sangue de rotina?",
                "Para a maioria dos exames de sangue de rotina, o jejum recomendado é de 8 a 12 horas...",
                "EXAMES",
                Arrays.asList("jejum", "exame de sangue", "horas jejum", "preparo exame")
        );
        faqExame.setId(2L);
    }

    @Test
    @DisplayName("Deve retornar resposta quando pergunta corresponde a FAQ por keyword")
    void testGetResponseWithKeywordMatch() {
        when(faqRepository.findAll()).thenReturn(Arrays.asList(faqAgendamento, faqExame));

        ChatbotResponse response = chatbotService.getResponse("Como agendar uma consulta?");

        assertThat(response).isNotNull();
        assertThat(response.matched()).isTrue();
        assertThat(response.answer()).contains("aplicativo");
        assertThat(response.category()).isEqualTo("AGENDAMENTO");
        assertThat(response.originalQuestion()).isNotNull();

        verify(faqRepository, times(1)).findAll();
        verify(unansweredRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar resposta com categoria e pergunta original")
    void testGetResponseReturnsMetadata() {
        when(faqRepository.findAll()).thenReturn(List.of(faqExame));

        ChatbotResponse response = chatbotService.getResponse("Jejum exame sangue?");

        assertThat(response.matched()).isTrue();
        assertThat(response.category()).isEqualTo("EXAMES");
        assertThat(response.originalQuestion()).contains("jejum");

        verify(faqRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve registrar pergunta não respondida quando nenhuma FAQ corresponde")
    void testGetResponseSavesUnansweredQuestion() {
        when(faqRepository.findAll()).thenReturn(Collections.emptyList());

        ChatbotResponse response = chatbotService.getResponse("Pergunta totalmente diferente");

        assertThat(response.matched()).isFalse();
        assertThat(response.answer()).contains("atendente");
        assertThat(response.category()).isNull();

        ArgumentCaptor<UnansweredQuestion> captor = ArgumentCaptor.forClass(UnansweredQuestion.class);
        verify(unansweredRepository, times(1)).save(captor.capture());
        UnansweredQuestion saved = captor.getValue();
        assertThat(saved.getUserMessage()).isEqualTo("Pergunta totalmente diferente");
    }

    @Test
    @DisplayName("Deve priorizar keywords sobre palavras da pergunta")
    void testKeywordPriorityOverQuestionWords() {
        FaqEntry faq1 = new FaqEntry(
                "Pergunta com marcar",
                "Resposta 1",
                "CAT1",
                Arrays.asList("agendar", "consulta")
        );
        faq1.setId(1L);

        FaqEntry faq2 = new FaqEntry(
                "Pergunta sobre marcar consulta",
                "Resposta 2",
                "CAT2",
                Collections.emptyList()
        );
        faq2.setId(2L);

        when(faqRepository.findAll()).thenReturn(Arrays.asList(faq1, faq2));

        ChatbotResponse response = chatbotService.getResponse("agendar consulta");

        assertThat(response.matched()).isTrue();
        assertThat(response.answer()).isEqualTo("Resposta 1");

        verify(faqRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve ser case-insensitive")
    void testCaseInsensitiveMatching() {
        when(faqRepository.findAll()).thenReturn(List.of(faqAgendamento));

        ChatbotResponse response = chatbotService.getResponse("COMO AGENDAR UMA CONSULTA??");

        assertThat(response.matched()).isTrue();
        assertThat(response.category()).isEqualTo("AGENDAMENTO");

        verify(faqRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve ignorar pontuação na busca")
    void testIgnorePunctuation() {
        when(faqRepository.findAll()).thenReturn(List.of(faqExame));

        ChatbotResponse response = chatbotService.getResponse("Jejum... exame; sangue!");

        assertThat(response.matched()).isTrue();
        assertThat(response.category()).isEqualTo("EXAMES");

        verify(faqRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar resposta padrão quando FAQ está vazia")
    void testEmptyFaqDatabase() {
        when(faqRepository.findAll()).thenReturn(Collections.emptyList());

        ChatbotResponse response = chatbotService.getResponse("Alguma pergunta");

        assertThat(response.matched()).isFalse();
        assertThat(response.answer()).contains("Desculpe");
        assertThat(response.answer()).contains("atendente");

        verify(unansweredRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Deve selecionar FAQ com maior score quando múltiplas correspondem")
    void testSelectFaqWithHighestScore() {
        FaqEntry lowScore = new FaqEntry(
                "Consulta",
                "Resposta com pouca relevância",
                "CAT1",
                Arrays.asList("consulta")
        );
        lowScore.setId(1L);

        FaqEntry highScore = new FaqEntry(
                "Como agendar uma consulta",
                "Resposta com alta relevância",
                "CAT2",
                Arrays.asList("agendar", "consulta", "clinico")
        );
        highScore.setId(2L);

        when(faqRepository.findAll()).thenReturn(Arrays.asList(lowScore, highScore));

        ChatbotResponse response = chatbotService.getResponse("agendar consulta clinico");

        assertThat(response.answer()).isEqualTo("Resposta com alta relevância");

        verify(faqRepository, times(1)).findAll();
    }
}
