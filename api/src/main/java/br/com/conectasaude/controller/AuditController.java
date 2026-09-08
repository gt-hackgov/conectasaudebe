package br.com.conectasaude.controller;

import br.com.conectasaude.dto.audit.AuditLogResponse;
import br.com.conectasaude.dto.common.PageResponse;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.repository.AuditLogRepository;
import br.com.conectasaude.service.AuditService;
import br.com.conectasaude.specification.AuditLogSpecification;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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
    public PageResponse<AuditLogResponse> listarLogs(
            @RequestParam(required = false) Role perfil,
            @RequestParam(required = false) AuditAction acao,
            @RequestParam(required = false) AuditResult resultado,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime inicio,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fim,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {

        int pagina = Math.max(page, 0);
        int tamanho = Math.min(Math.max(size, 1), 100);

        PageRequest pageable = PageRequest.of(
                pagina,
                tamanho,
                Sort.by(
                        Sort.Direction.DESC,
                        "registradoEm"
                )
        );

        Page<AuditLogResponse> logs = auditLogRepository
                .findAll(
                        AuditLogSpecification.comFiltros(
                                perfil,
                                acao,
                                resultado,
                                inicio,
                                fim
                        ),
                        pageable
                )
                .map(AuditLogResponse::fromEntity);

        UUID usuarioId = UUID.fromString(
                jwt.getSubject()
        );

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

        return PageResponse.from(logs);
    }
}