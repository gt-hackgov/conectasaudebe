package br.com.conectasaude.security;

import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.service.AuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class AuditedAccessDeniedHandler implements AccessDeniedHandler {

    private final AuditService auditService;

    public AuditedAccessDeniedHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        Authentication authentication =
                (Authentication) request.getUserPrincipal();

        UUID usuarioId = null;
        Role role = null;

        if (authentication != null
                && authentication.getPrincipal() instanceof Jwt jwt) {

            usuarioId = UUID.fromString(jwt.getSubject());

            String roleClaim = jwt.getClaimAsString("role");

            if (roleClaim != null) {
                role = Role.valueOf(roleClaim);
            }
        }

        try {
            auditService.registrar(
                    usuarioId,
                    role,
                    AuditAction.ACESSO_NEGADO,
                    "RECURSO_PROTEGIDO",
                    AuditResult.NEGADO,
                    request
            );
        } catch (Exception ignored) {
            // Uma falha na auditoria não deve alterar a resposta HTTP de acesso negado.
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"erro\":\"Acesso negado\"}"
        );
    }
}