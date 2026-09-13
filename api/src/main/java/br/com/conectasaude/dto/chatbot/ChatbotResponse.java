package br.com.conectasaude.dto.chatbot;

public record ChatbotResponse(
        String answer,
        boolean matched,
        String category,
        String originalQuestion
) {
    public ChatbotResponse(String answer, boolean matched) {
        this(answer, matched, null, null);
    }
}
