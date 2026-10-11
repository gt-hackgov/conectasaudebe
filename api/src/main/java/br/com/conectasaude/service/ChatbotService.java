package br.com.conectasaude.service;

import br.com.conectasaude.dto.chatbot.ChatbotResponse;
import br.com.conectasaude.model.FaqEntry;
import br.com.conectasaude.model.UnansweredQuestion;
import br.com.conectasaude.repository.FaqEntryRepository;
import br.com.conectasaude.repository.UnansweredQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ChatbotService {

    private final FaqEntryRepository faqRepository;
    private final UnansweredQuestionRepository unansweredRepository;

    public ChatbotService(FaqEntryRepository faqRepository,
                          UnansweredQuestionRepository unansweredRepository) {
        this.faqRepository = faqRepository;
        this.unansweredRepository = unansweredRepository;
    }

    public ChatbotResponse getResponse(String userMessage) {
        List<FaqEntry> entries = faqRepository.findAll();
        String normalized = normalize(userMessage);

        FaqEntry bestMatch = null;
        int bestScore = 0;

        for (FaqEntry entry : entries) {
            int score = calculateMatchScore(normalized, entry);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = entry;
            }
        }

        if (bestMatch != null && bestScore > 0) {
            return new ChatbotResponse(
                    bestMatch.getAnswer(),
                    true,
                    bestMatch.getCategory(),
                    bestMatch.getQuestion()
            );
        }

        // Registra pergunta não respondida para análise posterior
        saveUnansweredQuestion(userMessage);

        return new ChatbotResponse(
                "Desculpe, não consegui encontrar uma resposta para sua pergunta. Um atendente entrará em contato em breve. 😊",
                false,
                null,
                null
        );
    }

    @Transactional(readOnly = false)
    public void saveUnansweredQuestion(String userMessage) {
        unansweredRepository.save(new UnansweredQuestion(userMessage));
    }

    private int calculateMatchScore(String userMessage, FaqEntry entry) {
        int score = 0;

        // Verifica se alguma keyword está na mensagem
        if (entry.getKeywords() != null && !entry.getKeywords().isEmpty()) {
            for (String keyword : entry.getKeywords()) {
                if (userMessage.contains(normalize(keyword))) {
                    score += 2;
                }
            }
        }

        // Verifica se palavras da pergunta estão na mensagem
        if (entry.getQuestion() != null) {
            String[] words = normalize(entry.getQuestion()).split(" ");
            for (String word : words) {
                if (word.length() > 3 && userMessage.contains(word)) {
                    score++;
                }
            }
        }

        return score;
    }

    private String normalize(String text) {
        return text.toLowerCase()
                .trim()
                .replaceAll("[?,!;:\\.\\-]", "")
                .replaceAll("\\s+", " ");
    }
}
