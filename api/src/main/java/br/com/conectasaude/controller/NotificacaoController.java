package br.com.conectasaude.controller;

import br.com.conectasaude.dto.notificacao.NotificacaoResponse;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.service.AuditService;
import br.com.conectasaude.service.NotificacaoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final AuditService auditService;

    public NotificacaoController(
            NotificacaoService notificacaoService,
            AuditService auditService
    ) {
        this.notificacaoService = notificacaoService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        UUID usuarioId = UUID.fromString(jwt.getSubject());
        Role role = Role.valueOf(jwt.getClaimAsString("role"));

        List<NotificacaoResponse> notifications =
                notificacaoService.listarParaUsuario(usuarioId);

        auditService.registrar(
                usuarioId,
                role,
                AuditAction.VISUALIZAR_DADO_SENSIVEL,
                "NOTIFICACOES",
                AuditResult.SUCESSO,
                request
        );

        return ResponseEntity.ok(
                Map.of("notifications", notifications)
        );
    }
}
