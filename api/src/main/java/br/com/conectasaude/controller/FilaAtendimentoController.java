package br.com.conectasaude.controller;

import br.com.conectasaude.dto.fila.EntradaFilaRequest;
import br.com.conectasaude.dto.fila.EntradaFilaResponse;
import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.service.AuditService;
import br.com.conectasaude.service.FilaAtendimentoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Fila de atendimento por risco. Acesso restrito ao perfil MEDICO
 * (regra "/api/medico/**" no SecurityConfig).
 */
@RestController
@RequestMapping("/api/medico/fila")
public class FilaAtendimentoController {

    private static final String RECURSO = "FILA_ATENDIMENTO";

    private final FilaAtendimentoService filaService;
    private final AuditService auditService;

    public FilaAtendimentoController(FilaAtendimentoService filaService, AuditService auditService) {
        this.filaService = filaService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listar(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        List<EntradaFilaResponse> fila = filaService.listarEmOrdem();
        auditar(jwt, AuditAction.VISUALIZAR_DADO_SENSIVEL, AuditResult.SUCESSO, request);

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("fila", fila);
        corpo.put("total", fila.size());
        corpo.put("acimaDoTempoAlvo", fila.stream().filter(EntradaFilaResponse::acimaDoTempoAlvo).count());
        return ResponseEntity.ok(corpo);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> inserir(
            @Valid @RequestBody EntradaFilaRequest body,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        EntradaFilaResponse entrada = filaService.inserir(body);
        auditar(jwt, AuditAction.CRIAR_REGISTRO, AuditResult.SUCESSO, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Paciente adicionado à fila", "entrada", entrada));
    }

    @PostMapping("/chamar")
    public ResponseEntity<Map<String, Object>> chamarProximo(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        Optional<EntradaFilaResponse> chamado = filaService.chamarProximo();
        if (chamado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "A fila está vazia."));
        }
        auditar(jwt, AuditAction.ALTERAR_STATUS, AuditResult.SUCESSO, request);
        return ResponseEntity.ok(Map.of("message", "Paciente chamado", "chamado", chamado.get()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> remover(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        if (!filaService.remover(id)) {
            auditar(jwt, AuditAction.EXCLUIR_REGISTRO, AuditResult.NEGADO, request);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Paciente não encontrado na fila."));
        }
        auditar(jwt, AuditAction.EXCLUIR_REGISTRO, AuditResult.SUCESSO, request);
        return ResponseEntity.ok(Map.of("message", "Paciente removido da fila"));
    }

    private void auditar(Jwt jwt, AuditAction acao, AuditResult resultado, HttpServletRequest request) {
        auditService.registrar(
                UUID.fromString(jwt.getSubject()),
                Role.valueOf(jwt.getClaimAsString("role")),
                acao,
                RECURSO,
                resultado,
                request
        );
    }
}
