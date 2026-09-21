package br.com.conectasaude.controller;

import br.com.conectasaude.dto.chatbot.ChatbotRequest;
import br.com.conectasaude.dto.chatbot.ChatbotResponse;
import br.com.conectasaude.service.ChatbotService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatService = chatbotService;
    }

    @PostMapping("/message")
    public ChatbotResponse sendMessage(@RequestBody ChatbotRequest request) {
        return chatService.getResponse(request.getMessage());
    }
}

