package br.com.conectasaude.dto.chatbot;

import jakarta.validation.constraints.NotBlank;

public class ChatbotRequest {
    @NotBlank(message = "Mensagem não pode estar vazia")
    private String message;

    public ChatbotRequest() {}

    public ChatbotRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
