package br.com.conectasaude.service;

import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditLog;
import br.com.conectasaude.model.audit.AuditResult;
import br.com.conectasaude.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(
            AuditLogRepository auditLogRepository
    ) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(
            UUID usuarioId,
            Role perfil,
            AuditAction acao,
            String recurso,
            AuditResult resultado,
            HttpServletRequest request
    ) {

        AuditLog auditLog = new AuditLog(
                usuarioId,
                perfil,
                acao,
                recurso,
                resultado,
                request.getRemoteAddr(),
                request.getMethod(),
                request.getRequestURI()
        );

        auditLogRepository.save(auditLog);
    }
}