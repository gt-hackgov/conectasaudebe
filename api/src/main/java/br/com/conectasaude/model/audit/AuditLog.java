package br.com.conectasaude.model.audit;

import br.com.conectasaude.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(
                        name = "idx_audit_registrado_em",
                        columnList = "registrado_em"
                ),
                @Index(
                        name = "idx_audit_usuario_id",
                        columnList = "usuario_id"
                ),
                @Index(
                        name = "idx_audit_acao",
                        columnList = "acao"
                )
        }
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "registrado_em",
            nullable = false,
            updatable = false
    )
    private LocalDateTime registradoEm;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Role perfil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction acao;

    @Column(nullable = false, length = 100)
    private String recurso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditResult resultado;

    @Column(length = 45)
    private String ip;

    @Column(name = "metodo_http", length = 10)
    private String metodoHttp;

    @Column(length = 255)
    private String endpoint;

    public AuditLog() {
    }

    public AuditLog(
            UUID usuarioId,
            Role perfil,
            AuditAction acao,
            String recurso,
            AuditResult resultado,
            String ip,
            String metodoHttp,
            String endpoint
    ) {
        this.usuarioId = usuarioId;
        this.perfil = perfil;
        this.acao = acao;
        this.recurso = recurso;
        this.resultado = resultado;
        this.ip = ip;
        this.metodoHttp = metodoHttp;
        this.endpoint = endpoint;
    }

    @PrePersist
    public void prePersist() {
        this.registradoEm = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public Role getPerfil() {
        return perfil;
    }

    public AuditAction getAcao() {
        return acao;
    }

    public String getRecurso() {
        return recurso;
    }

    public AuditResult getResultado() {
        return resultado;
    }

    public String getIp() {
        return ip;
    }

    public String getMetodoHttp() {
        return metodoHttp;
    }

    public String getEndpoint() {
        return endpoint;
    }
}