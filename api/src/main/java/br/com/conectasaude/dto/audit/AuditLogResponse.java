package br.com.conectasaude.dto.audit;

import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditLog;
import br.com.conectasaude.model.audit.AuditResult;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        LocalDateTime registradoEm,
        UUID usuarioId,
        Role perfil,
        AuditAction acao,
        String recurso,
        AuditResult resultado,
        String ip,
        String metodoHttp,
        String endpoint
) {

    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getRegistradoEm(),
                auditLog.getUsuarioId(),
                auditLog.getPerfil(),
                auditLog.getAcao(),
                auditLog.getRecurso(),
                auditLog.getResultado(),
                auditLog.getIp(),
                auditLog.getMetodoHttp(),
                auditLog.getEndpoint()
        );
    }
}