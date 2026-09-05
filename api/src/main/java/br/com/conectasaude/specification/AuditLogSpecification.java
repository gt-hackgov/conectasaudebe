package br.com.conectasaude.specification;

import br.com.conectasaude.model.Role;
import br.com.conectasaude.model.audit.AuditAction;
import br.com.conectasaude.model.audit.AuditLog;
import br.com.conectasaude.model.audit.AuditResult;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {

    private AuditLogSpecification() {
    }

    public static Specification<AuditLog> comFiltros(
            Role perfil,
            AuditAction acao,
            AuditResult resultado,
            LocalDateTime inicio,
            LocalDateTime fim
    ) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (perfil != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("perfil"),
                                perfil
                        )
                );
            }

            if (acao != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("acao"),
                                acao
                        )
                );
            }

            if (resultado != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("resultado"),
                                resultado
                        )
                );
            }

            if (inicio != null) {
                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("registradoEm"),
                                inicio
                        )
                );
            }

            if (fim != null) {
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("registradoEm"),
                                fim
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}