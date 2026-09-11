package br.com.conectasaude.controller;

import br.com.conectasaude.dto.agendamento.AgendamentoRequest;
import br.com.conectasaude.dto.agendamento.AgendamentoResponse;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.service.AgendamentoService;
import br.com.conectasaude.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final AuditService auditService;

    public AgendamentoController(
            AgendamentoService agendamentoService,
            AuditService auditService
    ) {
        this.agendamentoService = agendamentoService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<?> getAppointments(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        UUID usuarioId = UUID.fromString(jwt.getSubject());
        Role role = Role.valueOf(jwt.getClaimAsString("role"));

        List<AgendamentoResponse> appointments =
                agendamentoService.listarPorUsuario(usuarioId);

        auditService.registrar(
                usuarioId,
                role,
                AuditAction.VISUALIZAR_DADO_SENSIVEL,
                "AGENDAMENTOS",
                AuditResult.SUCESSO,
                request
        );

        return ResponseEntity.ok(
                Map.of("appointments", appointments)
        );
    }

    @PostMapping
    public ResponseEntity<?> createAppointment(
            @Valid @RequestBody AgendamentoRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest
    ) {
        UUID usuarioId = UUID.fromString(jwt.getSubject());
        Role role = Role.valueOf(jwt.getClaimAsString("role"));

        AgendamentoResponse saved =
                agendamentoService.criar(request, usuarioId);

        auditService.registrar(
                usuarioId,
                role,
                AuditAction.CRIAR_REGISTRO,
                "AGENDAMENTOS",
                AuditResult.SUCESSO,
                httpRequest
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Agendamento criado com sucesso",
                        "appointment", saved
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        UUID usuarioId = UUID.fromString(jwt.getSubject());
        Role role = Role.valueOf(jwt.getClaimAsString("role"));

        if (!agendamentoService.excluir(id, usuarioId)) {
            auditService.registrar(
                    usuarioId,
                    role,
                    AuditAction.EXCLUIR_REGISTRO,
                    "AGENDAMENTOS",
                    AuditResult.NEGADO,
                    request
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Consulta não encontrada."));
        }

        auditService.registrar(
                usuarioId,
                role,
                AuditAction.EXCLUIR_REGISTRO,
                "AGENDAMENTOS",
                AuditResult.SUCESSO,
                request
        );

        return ResponseEntity.ok(
                Map.of("message", "Consulta cancelada com sucesso")
        );
    }
}
