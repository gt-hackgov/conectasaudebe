package br.com.conectasaude.controller;

import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.service.AuditService;
import br.com.conectasaude.service.IndicadoresService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/** Indicadores de gestão. Acesso restrito ao ADMIN (regra "/api/admin/**" no SecurityConfig). */
@RestController
@RequestMapping("/api/admin/indicadores")
public class IndicadoresController {

    private final IndicadoresService indicadoresService;
    private final AuditService auditService;

    public IndicadoresController(IndicadoresService indicadoresService, AuditService auditService) {
        this.indicadoresService = indicadoresService;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> indicadores(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        Map<String, Object> resposta = indicadoresService.calcular();
        auditService.registrar(
                UUID.fromString(jwt.getSubject()),
                Role.valueOf(jwt.getClaimAsString("role")),
                AuditAction.ACESSAR_AREA_ADMINISTRATIVA,
                "INDICADORES",
                AuditResult.SUCESSO,
                request
        );
        return ResponseEntity.ok(resposta);
    }
}
