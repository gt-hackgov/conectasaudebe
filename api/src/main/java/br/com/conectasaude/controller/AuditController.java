package br.com.conectasaude.controller;

import br.com.conectasaude.dto.audit.AuditLogResponse;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.repository.AuditLogRepository;
import br.com.conectasaude.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auditoria")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    public AuditController(
            AuditLogRepository auditLogRepository,
            AuditService auditService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    @GetMapping("/logs")
    public List<AuditLogResponse> listarLogs(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {

        List<AuditLogResponse> logs = auditLogRepository
                .findTop100ByOrderByRegistradoEmDesc()
                .stream()
                .map(AuditLogResponse::fromEntity)
                .toList();

        UUID usuarioId = UUID.fromString(jwt.getSubject());
        Role role = Role.valueOf(
                jwt.getClaimAsString("role")
        );

        auditService.registrar(
                usuarioId,
                role,
                AuditAction.CONSULTAR_AUDITORIA,
                "TRILHA_AUDITORIA",
                AuditResult.SUCESSO,
                request
        );

        return logs;
    }
}