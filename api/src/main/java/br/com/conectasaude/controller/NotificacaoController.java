package br.com.conectasaude.controller;

import br.com.conectasaude.model.Notificacao;
import br.com.conectasaude.repository.NotificacaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificacaoController {

    private final NotificacaoRepository repository;

    public NotificacaoController(NotificacaoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications() {
        List<Notificacao> list = repository.findAll();
        return ResponseEntity.ok(Map.of("notifications", list));
    }
}
